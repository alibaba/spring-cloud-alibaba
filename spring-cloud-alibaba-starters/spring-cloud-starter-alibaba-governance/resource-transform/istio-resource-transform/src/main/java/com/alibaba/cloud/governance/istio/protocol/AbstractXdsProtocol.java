/*
 * Copyright 2013-2018 the original author or authors.
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.alibaba.cloud.governance.istio.NodeBuilder;
import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsScheduledThreadPool;
import io.envoyproxy.envoy.config.core.v3.Node;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractXdsProtocol<T> implements XdsProtocol<T> {

	protected static final Logger log = LoggerFactory
			.getLogger(AbstractXdsProtocol.class);

	protected XdsChannel xdsChannel;

	protected final Node node = NodeBuilder.getNode();

	protected int pollingTime;

	private XdsScheduledThreadPool xdsScheduledThreadPool;

	private boolean isPolling;

	private final Map<Long, StreamObserver<DiscoveryRequest>> requestObserverMap = new ConcurrentHashMap<>();

	private final Map<Long, CompletableFuture<List<T>>> futureMap = new ConcurrentHashMap<>();

	private final Map<Long, Set<String>> requestResource = new ConcurrentHashMap<>();

	protected final static AtomicLong requestId = new AtomicLong(0);

	private static final int DEFAULT_POLLING_TIME = 30;

	public AbstractXdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool, int pollingTime) {
		this.xdsChannel = xdsChannel;
		this.xdsScheduledThreadPool = xdsScheduledThreadPool;
		this.pollingTime = pollingTime <= 0 ? DEFAULT_POLLING_TIME : pollingTime;
	}

	@Override
	public long observeResource(Set<String> resourceNames, Consumer<List<T>> consumer) {
		long id = requestId.getAndDecrement();
		if (resourceNames == null) {
			resourceNames = new HashSet<>();
		}
		requestResource.put(id, resourceNames);
		try {
			consumer.accept(doGetResource(id, resourceNames, consumer));
		}
		catch (Exception e) {
			log.error("error on get observe resource from xds", e);
		}
		if (!isPolling) {
			xdsScheduledThreadPool.scheduleAtFixedRate(() -> {
				try {
					consumer.accept(doGetResource(id, requestResource.get(id), consumer));
				}
				catch (Exception e) {
					log.error("error on get observe resource from xds", e);
				}
			}, pollingTime, pollingTime, TimeUnit.SECONDS);
			isPolling = true;
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
			requestObserverMap.put(id, requestObserver);
		}
		sendXdsRequest(requestObserver, resourceNames);
		try {
			return future.get();
		}
		catch (ExecutionException | InterruptedException e) {
			return null;
		}
		finally {
			futureMap.remove(id);
		}
	}

	protected abstract List<T> decodeXdsResponse(DiscoveryResponse response);

	protected abstract void clearCache();

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

	private class XdsObserver implements StreamObserver<DiscoveryResponse> {

		private Consumer<List<T>> consumer;

		private long id;

		XdsObserver(long id, Consumer<List<T>> consumer) {
			this.id = id;
			this.consumer = consumer;
		}

		@Override
		public void onNext(DiscoveryResponse discoveryResponse) {
			log.info("receive notification from xds server, type: " + getTypeUrl()
					+ " requestId: " + id);
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
			log.error("connect to xds server failed", throwable);
			CompletableFuture<List<T>> future = futureMap.get(id);
			if (future != null) {
				future.complete(null);
				futureMap.remove(id);
			}
			requestResource.remove(id);
			requestObserverMap.put(id,
					xdsChannel.createDiscoveryRequest(new XdsObserver(id, consumer)));
		}

		@Override
		public void onCompleted() {
			log.info("xds connect completed");
		}

	}

}
