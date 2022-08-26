package com.alibaba.cloud.governance.istio.protocol;

import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.common.rules.util.NodeBuilder;
import io.envoyproxy.envoy.config.core.v3.Node;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public abstract class AbstractXdsProtocol<T> implements XdsProtocol<T> {

	private static final Logger log = LoggerFactory.getLogger(AbstractXdsProtocol.class);

	protected final XdsChannel xdsChannel;

	protected final Node node;

	protected final XdsConfigProperties xdsConfigProperties;

	private final ScheduledThreadPoolExecutor pollingExecutor;

	private final Map<Long, StreamObserver<DiscoveryRequest>> requestObserverMap = new ConcurrentHashMap<>();

	private final Map<Long, CompletableFuture<List<T>>> futureMap = new ConcurrentHashMap<>();

	private final Map<Long, Set<String>> resourceMap = new ConcurrentHashMap<>();

	protected final static AtomicLong requestId = new AtomicLong(0);

	private class XdsObserver implements StreamObserver<DiscoveryResponse> {

		private Consumer<List<T>> consumer;

		private long id;

		public XdsObserver(long id, Consumer<List<T>> consumer) {
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
			resourceMap.remove(id);
			requestObserverMap.remove(id);
		}

		@Override
		public void onCompleted() {
			log.info("xds connect completed");
		}

	}

	public AbstractXdsProtocol(XdsChannel xdsChannel,
			XdsConfigProperties xdsConfigProperties) {
		this.xdsChannel = xdsChannel;
		this.xdsConfigProperties = xdsConfigProperties;
		this.node = NodeBuilder.getNode();
		this.pollingExecutor = new ScheduledThreadPoolExecutor(
				xdsConfigProperties.getPollingPoolSize());
	}

	@Override
	public long observeResource(Set<String> resourceNames, Consumer<List<T>> consumer) {
		long id = requestId.getAndDecrement();
		try {
			consumer.accept(doGetResource(id, resourceNames, consumer));
		}
		catch (Exception e) {
			log.error("error on get observe resource from xds", e);
		}
		pollingExecutor.scheduleAtFixedRate(() -> {
			try {
				consumer.accept(doGetResource(id, resourceNames, consumer));
			}
			catch (Exception e) {
				log.error("error on get observe resource from xds", e);
			}
		}, xdsConfigProperties.getPollingTimeout(),
				xdsConfigProperties.getPollingTimeout(), TimeUnit.SECONDS);
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
		resourceMap.put(id, resourceNames);
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
				.addAllResourceNames(resourceMap.get(id) == null ? new ArrayList<>()
						: resourceMap.get(id))
				.setTypeUrl(response.getTypeUrl()).setResponseNonce(response.getNonce())
				.build();
		observer.onNext(request);
		resourceMap.remove(id);
	}

}
