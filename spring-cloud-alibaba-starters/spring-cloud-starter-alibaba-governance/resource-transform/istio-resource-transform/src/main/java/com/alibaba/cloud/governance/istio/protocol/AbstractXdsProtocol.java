package com.alibaba.cloud.governance.istio.protocol;

import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.common.rules.util.NodeBuilder;
import io.envoyproxy.envoy.config.core.v3.Node;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.stub.StreamObserver;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public abstract class AbstractXdsProtocol<T> implements XdsProtocol<T> {

	protected final XdsChannel xdsChannel;

	protected final Node node;

	protected final XdsConfigProperties xdsConfigProperties;

	private final ScheduledThreadPoolExecutor pollingExecutor;

	private final Map<Long, StreamObserver<DiscoveryRequest>> requestObserverMap = new ConcurrentHashMap<>();

	private final Map<Long, CompletableFuture<List<T>>> futureMap = new ConcurrentHashMap<>();

	private final Map<Long, Set<String>> resourceMap = new ConcurrentHashMap<>();

	protected final static AtomicLong requestId = new AtomicLong(0);

	private class XdsObserver implements StreamObserver<DiscoveryResponse> {

		private long id;

		public XdsObserver(long id) {
			this.id = id;
		}

		@Override
		public void onNext(DiscoveryResponse discoveryResponse) {
			List<T> t = decodeXdsResponse(discoveryResponse);
			CompletableFuture<List<T>> future = futureMap.get(id);
			if (future == null) {
				return;
			}
			future.complete(t);
			sendAckRequest(id, discoveryResponse);
		}

		@Override
		public void onError(Throwable throwable) {

		}

		@Override
		public void onCompleted() {

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
			consumer.accept(doGetSource(id, resourceNames));
		}
		catch (Exception e) {
			// TODO: print log
		}
		pollingExecutor.scheduleAtFixedRate(() -> {
			try {
				consumer.accept(doGetSource(id, resourceNames));
			}
			catch (Exception e) {
				// TODO: print log
			}
		}, xdsConfigProperties.getPollingTimeout(),
				xdsConfigProperties.getPollingTimeout(), TimeUnit.SECONDS);
		return id;
	}

	@Override
	public List<T> getResource(Set<String> resourceNames) {
		long id = requestId.getAndDecrement();
		return doGetSource(id, resourceNames);
	}

	private List<T> doGetSource(long id, Set<String> resourceNames) {
		if (resourceNames == null) {
			resourceNames = new HashSet<>();
		}
		CompletableFuture<List<T>> future = new CompletableFuture<>();
		futureMap.put(id, future);
		StreamObserver<DiscoveryRequest> requestObserver = xdsChannel
				.createDiscoveryRequest(new XdsObserver(id));
		sendXdsRequest(requestObserver, resourceNames);
		try {
			return future.get();
		}
		catch (ExecutionException | InterruptedException e) {
			return null;
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
				.addAllResourceNames(resourceMap.get(id))
				.setTypeUrl(response.getTypeUrl()).setResponseNonce(response.getNonce())
				.build();
		observer.onNext(request);
	}

}
