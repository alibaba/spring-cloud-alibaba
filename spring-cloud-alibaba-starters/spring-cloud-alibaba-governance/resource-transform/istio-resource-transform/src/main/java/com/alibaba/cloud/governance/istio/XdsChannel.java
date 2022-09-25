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

package com.alibaba.cloud.governance.istio;

import java.io.ByteArrayInputStream;

import io.envoyproxy.envoy.service.discovery.v3.AggregatedDiscoveryServiceGrpc;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XdsChannel implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(XdsChannel.class);

	private ManagedChannel channel;

	public XdsChannel(XdsConfigProperties xdsConfigProperties) {
		try {
			if (xdsConfigProperties.isSecure()) {
				SslContext sslcontext = GrpcSslContexts.forClient()
						// if server's cert doesn't chain to a standard root
						.trustManager(InsecureTrustManagerFactory.INSTANCE)
						// TODO: fill the publicKey and privateKey
						.keyManager(new ByteArrayInputStream("".getBytes()),
								new ByteArrayInputStream("".getBytes()))
						.build();
				this.channel = NettyChannelBuilder
						.forTarget(xdsConfigProperties.getHost() + ":"
								+ xdsConfigProperties.getPort())
						.negotiationType(NegotiationType.TLS).sslContext(sslcontext)
						.build();
			}
			else {
				this.channel = NettyChannelBuilder
						.forTarget(xdsConfigProperties.getHost() + ":"
								+ xdsConfigProperties.getPort())
						.negotiationType(NegotiationType.PLAINTEXT).build();
			}
		}
		catch (Exception e) {
			log.error("create XdsChannel failed", e);
		}
	}

	@Override
	public void close() throws Exception {
		if (channel != null) {
			channel.shutdown();
		}
	}

	public StreamObserver<DiscoveryRequest> createDiscoveryRequest(
			StreamObserver<DiscoveryResponse> observer) {
		return AggregatedDiscoveryServiceGrpc.newStub(channel)
				.streamAggregatedResources(observer);
	}

}
