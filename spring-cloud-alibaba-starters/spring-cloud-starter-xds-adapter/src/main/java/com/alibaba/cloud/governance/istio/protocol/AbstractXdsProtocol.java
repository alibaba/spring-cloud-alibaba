/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.governance.istio.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.alibaba.cloud.governance.istio.NodeBuilder;
import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.XdsScheduledThreadPool;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.governance.istio.filter.XdsResolveFilter;
import io.envoyproxy.envoy.config.core.v3.Node;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public abstract class AbstractXdsProtocol<T>
		implements XdsProtocol<T>, ApplicationContextAware {

	protected static final Logger log = LoggerFactory
			.getLogger(AbstractXdsProtocol.class);

	protected XdsChannel xdsChannel;

	protected final Node node = NodeBuilder.getNode();

	protected XdsConfigProperties xdsConfigProperties;

	protected List<XdsResolveFilter<List<T>>> filters = new ArrayList<>();

	private Set<String> resourceNames = new HashSet<>();

	private final XdsScheduledThreadPool xdsScheduledThreadPool;

	/**
	 * does the protocol need polling.
	 */
	private boolean needPolling;

	/**
	 * send event to submodules.
	 */
	protected ApplicationContext applicationContext;

	private final Map<Long, StreamObserver<DiscoveryRequest>> requestObserverMap = new ConcurrentHashMap<>();

	private final Map<Long, CompletableFuture<List<T>>> futureMap = new ConcurrentHashMap<>();

	private final Map<Long, Set<String>> requestResource = new ConcurrentHashMap<>();

	protected final static AtomicLong requestId = new AtomicLong(0);

	public AbstractXdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool,
			XdsConfigProperties xdsConfigProperties) {
		this.xdsChannel = xdsChannel;
		this.xdsScheduledThreadPool = xdsScheduledThreadPool;
		this.xdsConfigProperties = xdsConfigProperties;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setNeedPolling(boolean needPolling) {
		this.needPolling = needPolling;
	}

	@Override
	public synchronized long observeResource(Set<String> resourceNames,
			Consumer<List<T>> consumer) {
		long id = getDefaultRequestId();
		if (resourceNames == null) {
			resourceNames = new HashSet<>();
		}
		requestResource.put(id, resourceNames);
		try {
			consumer.accept(doGetResource(id, resourceNames, consumer));
		}
		catch (Exception e) {
			log.error("Error on get observe resource from xds", e);
		}
		if (needPolling) {
			xdsScheduledThreadPool.scheduleAtFixedRate(() -> {
				try {
					consumer.accept(doGetResource(id, requestResource.get(id), consumer));
				}
				catch (Exception e) {
					log.error("Error on get observe resource from xds", e);
				}
			}, xdsConfigProperties.getPollingTime(), xdsConfigProperties.getPollingTime(),
					TimeUnit.SECONDS);
			needPolling = false;
		}
		return id;
	}

	@Override
	public List<T> getResource(Set<String> resourceNames) {
		long id = requestId.getAndDecrement();
		List<T> source = doGetResource(id, resourceNames, null);
		requestObserverMap.remove(id);
		return source;
	}

	public Set<String> getResourceNames() {
		return resourceNames;
	}

	private List<T> doGetResource(long id, Set<String> resourceNames,
			Consumer<List<T>> consumer) {
		if (resourceNames == null) {
			resourceNames = new HashSet<>();
		}
		CompletableFuture<List<T>> future = new CompletableFuture<>();
		futureMap.put(id, future);
		StreamObserver<DiscoveryRequest> requestObserver = requestObserverMap.get(id);
		if (requestObserver == null) {
			// reuse observer
			requestObserver = xdsChannel
					.createDiscoveryRequest(new XdsObserver(id, consumer));
			// requestObserver may be null when testing
			if (requestObserver != null) {
				requestObserverMap.put(id, requestObserver);
			}
		}
		sendXdsRequest(requestObserverMap.get(id), resourceNames);
		try {
			return future.get(xdsConfigProperties.getPollingTime(), TimeUnit.SECONDS);
		}
		catch (Exception e) {
			log.error("Failed to send Xds request", e);
			return Collections.emptyList();
		}
		finally {
			futureMap.remove(id);
		}
	}

	protected abstract List<T> decodeXdsResponse(DiscoveryResponse response);

	protected Set<String> resolveResourceNames(List<T> resources) {
		return new HashSet<>();
	}

	protected void fireXdsFilters(List<T> resources) {
		try {
			this.resourceNames = resolveResourceNames(resources);
		}
		catch (Exception e) {
			log.error("Error on resolving resource names from {}", resources);
		}
		for (XdsResolveFilter<List<T>> filter : filters) {
			try {
				if (!filter.resolve(resources)) {
					return;
				}
			}
			catch (Exception e) {
				log.error("Error on executing Xds filter {}", filter.getClass().getName(),
						e);
			}
		}

	}

	private void sendXdsRequest(StreamObserver<DiscoveryRequest> observer,
			Set<String> resourceNames) {
		DiscoveryRequest request = DiscoveryRequest.newBuilder().setNode(node)
				.setTypeUrl(getTypeUrl()).addAllResourceNames(resourceNames).build();
		observer.onNext(request);
	}

	private void sendAckRequest(long id, DiscoveryResponse response) {
		StreamObserver<DiscoveryRequest> observer = requestObserverMap.get(id);
		if (observer == null) {
			return;
		}
		DiscoveryRequest request = DiscoveryRequest.newBuilder()
				.setVersionInfo(response.getVersionInfo()).setNode(node)
				.addAllResourceNames(requestResource.get(id) == null ? new ArrayList<>()
						: requestResource.get(id))
				.setTypeUrl(response.getTypeUrl()).setResponseNonce(response.getNonce())
				.build();
		observer.onNext(request);
	}

	private int getDefaultRequestId() {
		switch (getTypeUrl()) {
		case IstioConstants.CDS_URL:
			return -1;
		case IstioConstants.EDS_URL:
			return -2;
		case IstioConstants.LDS_URL:
			return -3;
		case IstioConstants.RDS_URL:
			return -4;
		}
		throw new UnsupportedOperationException("Unknown type url");
	}

	private class XdsObserver implements StreamObserver<DiscoveryResponse> {

		private Consumer<List<T>> consumer;

		private long id;

		XdsObserver(long id, Consumer<List<T>> consumer) {
			this.id = id;
			this.consumer = consumer;
		}

		@Override
		public void onNext(DiscoveryResponse discoveryResponse) {
			if (xdsChannel == null) {
				return;
			}
			if (xdsConfigProperties.isLogXds()) {
				log.info("Receive notification from xds server, type: " + getTypeUrl()
						+ " requestId: " + id);
			}
			List<T> responses = decodeXdsResponse(discoveryResponse);
			CompletableFuture<List<T>> future = futureMap.get(id);
			if (future == null) {
				// means it is push operation from xds, consume it directly
				consumer.accept(responses);
				sendAckRequest(id, discoveryResponse);
				return;
			}
			future.complete(responses);
			sendAckRequest(id, discoveryResponse);
		}

		@Override
		public void onError(Throwable throwable) {
			if (xdsChannel == null) {
				return;
			}
			if (xdsConfigProperties.isLogXds()) {
				log.error("Connect to xds server failed, reconnecting", throwable);
			}
			CompletableFuture<List<T>> future = futureMap.get(id);
			if (future != null) {
				future.complete(null);
				futureMap.remove(id);
			}
			requestResource.remove(id);
			// refresh token again
			xdsChannel.refreshIstiodToken();
			// reconnected immediately
			StreamObserver<DiscoveryRequest> observer = xdsChannel
					.createDiscoveryRequest(new XdsObserver(id, consumer));
			if (observer != null) {
				requestObserverMap.put(id, observer);
			}
		}

		@Override
		public void onCompleted() {
			log.info("Xds connect completed");
		}

	}

}
