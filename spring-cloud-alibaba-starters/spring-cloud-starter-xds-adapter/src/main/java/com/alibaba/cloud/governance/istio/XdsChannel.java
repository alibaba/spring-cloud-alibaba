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

import java.io.ByteArrayInputStream;
import java.net.URI;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLException;

import com.alibaba.cloud.governance.istio.bootstrap.Bootstrapper;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.governance.istio.exception.XdsInitializationException;
import com.alibaba.cloud.governance.istio.sds.CertPair;
import com.alibaba.cloud.governance.istio.sds.IstioCertPairManager;
import io.envoyproxy.envoy.config.core.v3.Node;
import io.envoyproxy.envoy.service.discovery.v3.AggregatedDiscoveryServiceGrpc;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollDomainSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.unix.DomainSocketAddress;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.alibaba.cloud.governance.istio.constant.IstioConstants.ISTIOD_SECURE_PORT;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class XdsChannel implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(XdsChannel.class);

	private ManagedChannel channel;

	private String istiodToken;

	private final XdsConfigProperties xdsConfigProperties;

	private final Node node;

	private final IstioCertPairManager istioCertPairManager;

	private final Bootstrapper.BootstrapInfo bootstrapInfo;

	public XdsChannel(XdsConfigProperties xdsConfigProperties,
			IstioCertPairManager istioCertPairManager,
			Bootstrapper.BootstrapInfo bootstrapInfo) {
		this.xdsConfigProperties = xdsConfigProperties;
		this.istioCertPairManager = istioCertPairManager;
		this.bootstrapInfo = bootstrapInfo;
		try {
			if (Boolean.FALSE.equals(xdsConfigProperties.getUseAgent())) {
				this.channel = createIstioManagedChannel();
				this.node = NodeBuilder.getNode(xdsConfigProperties);
			}
			else {
				if (bootstrapInfo == null) {
					throw new XdsInitializationException(
							"No bootstrap info while using pilot agent");
				}
				this.channel = createPilotAgentManagedChannel();
				this.node = bootstrapInfo.node();
			}
			if (this.channel == null) {
				throw new XdsInitializationException(
						"Failed to create ManagedChannel while initializing");
			}
		}
		catch (Exception e) {
			throw new XdsInitializationException("Init xds channel failed", e);
		}
	}

	private ManagedChannel createIstioManagedChannel() throws SSLException {
		if (xdsConfigProperties.getPort() == ISTIOD_SECURE_PORT) {
			this.refreshIstiodToken();
			CertPair certPair = istioCertPairManager.getCertPair();
			if (certPair == null) {
				throw new XdsInitializationException(
						"Unable to init XdsChannel, failed to fetch certificate from CA");
			}
			SslContext sslcontext = GrpcSslContexts.forClient()
					// if server's cert doesn't chain to a standard root
					.trustManager(InsecureTrustManagerFactory.INSTANCE)
					.keyManager(
							new ByteArrayInputStream(certPair.getRawCertificateChain()),
							new ByteArrayInputStream(certPair.getRawPrivateKey()))
					.build();
			return NettyChannelBuilder
					.forTarget(xdsConfigProperties.getHost() + ":"
							+ xdsConfigProperties.getPort())
					.negotiationType(NegotiationType.TLS).sslContext(sslcontext).build();
		}
		else {
			return NettyChannelBuilder
					.forTarget(xdsConfigProperties.getHost() + ":"
							+ xdsConfigProperties.getPort())
					.negotiationType(NegotiationType.PLAINTEXT).build();
		}
	}

	private ManagedChannel createPilotAgentManagedChannel() {
		if (bootstrapInfo == null) {
			return null;
		}
		EpollEventLoopGroup elg = new EpollEventLoopGroup();
		return NettyChannelBuilder
				.forAddress(new DomainSocketAddress(
						URI.create(bootstrapInfo.servers().get(0).target()).getPath()))
				.eventLoopGroup(elg).channelType(EpollDomainSocketChannel.class)
				.usePlaintext().build();
	}

	private void refreshIstiodToken() {
		this.istiodToken = xdsConfigProperties.getIstiodToken();
		if (this.istiodToken == null) {
			throw new UnsupportedOperationException(
					"Unable to found kubernetes service account token file. "
							+ "Please check if work in Kubernetes and mount service account token file correctly.");
		}
	}

	@PreDestroy
	@Override
	public void close() {
		if (channel != null) {
			log.warn("Xds channel closing!");
			channel.shutdown();
		}
	}

	public void restart() {
		try {
			close();
			// refresh token again
			if (!xdsConfigProperties.getUseAgent() && xdsConfigProperties
					.getPort() == IstioConstants.ISTIOD_SECURE_PORT) {
				refreshIstiodToken();
			}
			if (istioCertPairManager != null) {
				this.channel = createIstioManagedChannel();
			}
			else if (bootstrapInfo != null) {
				this.channel = createPilotAgentManagedChannel();
			}
		}
		catch (Exception e) {
			log.error("Failed to restart xds channel", e);
		}
	}

	public StreamObserver<DiscoveryRequest> createDiscoveryRequest(
			StreamObserver<DiscoveryResponse> observer) {
		if (channel == null) {
			return null;
		}
		AggregatedDiscoveryServiceGrpc.AggregatedDiscoveryServiceStub stub = AggregatedDiscoveryServiceGrpc
				.newStub(channel);
		Metadata header = new Metadata();
		Metadata.Key<String> key = Metadata.Key.of("authorization",
				Metadata.ASCII_STRING_MARSHALLER);
		header.put(key, "Bearer " + this.istiodToken);
		stub = MetadataUtils.attachHeaders(stub, header);
		return stub.streamAggregatedResources(observer);
	}

	public Node getNode() {
		return node;
	}

}
