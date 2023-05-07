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

package com.alibaba.cloud.governance.istio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1 AggregateDiscoveryService is used to send xds request and handle xds
 * response.
 */
public class AggregateDiscoveryService {

	private static final Logger log = LoggerFactory
			.getLogger(AggregateDiscoveryService.class);

	private final Map<String, AbstractXdsProtocol> protocolMap = new HashMap<>();

	private final Map<String, Set<String>> requestResource = new ConcurrentHashMap<>();

	private StreamObserver<DiscoveryRequest> observer;

	private final XdsConfigProperties xdsConfigProperties;

	private final XdsChannel xdsChannel;

	private final ScheduledExecutorService retry;

	public AggregateDiscoveryService(XdsChannel xdsChannel,
			XdsConfigProperties xdsConfigProperties) {
		this.xdsChannel = xdsChannel;
		this.xdsConfigProperties = xdsConfigProperties;
		this.observer = xdsChannel.createDiscoveryRequest(new XdsObserver());
		this.retry = Executors.newSingleThreadScheduledExecutor();
	}

	public void addProtocol(AbstractXdsProtocol abstractXdsProtocol) {
		protocolMap.put(abstractXdsProtocol.getTypeUrl(), abstractXdsProtocol);
	}

	public void sendXdsRequest(String typeUrl, Set<String> resourceNames) {
		requestResource.put(typeUrl, resourceNames);
		DiscoveryRequest request = DiscoveryRequest.newBuilder()
				.setNode(xdsChannel.getNode()).setTypeUrl(typeUrl)
				.addAllResourceNames(resourceNames).build();
		observer.onNext(request);
	}

	private void sendAckRequest(DiscoveryResponse response) {
		Set<String> ackResource = requestResource.getOrDefault(response.getTypeUrl(),
				new HashSet<>());
		DiscoveryRequest request = DiscoveryRequest.newBuilder()
				.setVersionInfo(response.getVersionInfo()).setNode(xdsChannel.getNode())
				.addAllResourceNames(ackResource).setTypeUrl(response.getTypeUrl())
				.setResponseNonce(response.getNonce()).build();
		observer.onNext(request);
	}

	@PreDestroy
	public void close() {
		retry.shutdownNow();
	}

	private class XdsObserver implements StreamObserver<DiscoveryResponse> {

		@Override
		public void onNext(DiscoveryResponse discoveryResponse) {
			String typeUrl = discoveryResponse.getTypeUrl();
			if (xdsConfigProperties.isLogXds()) {
				log.info("Receive notification from xds server, type: {}, size: {}",
						typeUrl, discoveryResponse.getResourcesCount());
			}
			AbstractXdsProtocol protocol = protocolMap.get(typeUrl);
			if (protocol == null) {
				throw new UnsupportedOperationException("No protocol of type " + typeUrl);
			}
			List<?> responses = protocol.decodeXdsResponse(discoveryResponse);
			sendAckRequest(discoveryResponse);
			protocol.onResponseDecoded(responses);
		}

		@Override
		public void onError(Throwable throwable) {
			if (xdsConfigProperties.isLogXds()) {
				log.error("Connect to xds server failed, reconnect after 3 seconds",
						throwable);
			}

			requestResource.clear();
			// refresh token again
			if (!xdsConfigProperties.getUseAgent() && xdsConfigProperties
					.getPort() == IstioConstants.ISTIOD_SECURE_PORT) {
				xdsChannel.refreshIstiodToken();
			}
			retry.schedule(() -> {
				observer = xdsChannel.createDiscoveryRequest(this);
				sendXdsRequest(IstioConstants.CDS_URL, new HashSet<>());
				log.info("Reconnecting to istio control plane!");
			}, 3, TimeUnit.SECONDS);
		}

		@Override
		public void onCompleted() {
			log.info("Xds connect completed");
		}

	}

}
