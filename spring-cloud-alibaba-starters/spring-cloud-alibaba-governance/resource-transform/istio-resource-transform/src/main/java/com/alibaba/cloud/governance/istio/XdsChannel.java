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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.alibaba.cloud.commons.io.FileUtils;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import io.envoyproxy.envoy.service.discovery.v3.AggregatedDiscoveryServiceGrpc;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 */
public class XdsChannel implements AutoCloseable {

	private static final int ISTIOD_SECURE_PORT = 15012;

	private static final Logger log = LoggerFactory.getLogger(XdsChannel.class);

	private ManagedChannel channel;

	private String istiodToken;

	public XdsChannel(XdsConfigProperties xdsConfigProperties) {
		try {
			if (xdsConfigProperties.getPort() == ISTIOD_SECURE_PORT) {
				// fetch token first
				if (StringUtils.isNotEmpty(xdsConfigProperties.getIstiodToken())) {
					istiodToken = xdsConfigProperties.getIstiodToken();
				}
				else {
					istiodToken = this.fetchIstiodToken();
				}
				SslContext sslcontext = GrpcSslContexts.forClient()
						// if server's cert doesn't chain to a standard root
						.trustManager(InsecureTrustManagerFactory.INSTANCE)
						// TODO: fill the publicKey and privateKey
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

	private String fetchIstiodToken() {
		File saFile = new File(IstioConstants.KUBERNETES_SA_PATH);
		if (saFile.canRead()) {
			try {
				return FileUtils.readFileToString(saFile, StandardCharsets.UTF_8);
			}
			catch (IOException e) {
				log.error("Unable to read token file", e);
			}
		}
		if (this.istiodToken == null) {
			throw new UnsupportedOperationException(
					"Unable to found kubernetes service account token file. "
							+ "Please check if work in Kubernetes and mount service account token file correctly.");
		}
		return null;
	}

	@Override
	public void close() {
		if (channel != null) {
			channel.shutdown();
		}
	}

	public StreamObserver<DiscoveryRequest> createDiscoveryRequest(
			StreamObserver<DiscoveryResponse> observer) {
		AggregatedDiscoveryServiceGrpc.AggregatedDiscoveryServiceStub stub = AggregatedDiscoveryServiceGrpc
				.newStub(channel);
		Metadata header = new Metadata();
		Metadata.Key<String> key = Metadata.Key.of("authorization",
				Metadata.ASCII_STRING_MARSHALLER);
		header.put(key, "Bearer " + this.istiodToken);
		stub = MetadataUtils.attachHeaders(stub, header);
		return stub.streamAggregatedResources(observer);
	}

}
