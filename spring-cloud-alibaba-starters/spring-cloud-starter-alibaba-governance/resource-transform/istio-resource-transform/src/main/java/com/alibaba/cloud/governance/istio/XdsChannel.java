package com.alibaba.cloud.governance.istio;

import io.envoyproxy.envoy.service.discovery.v3.AggregatedDiscoveryServiceGrpc;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.x509.X509CertImpl;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

public class XdsChannel implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(XdsChannel.class);

	private ManagedChannel channel;

	public XdsChannel(XdsConfigProperties xdsConfigProperties) {
		try {
			if (xdsConfigProperties.isSecure()) {
				SslContext sslcontext = GrpcSslContexts.forClient()
						// if server's cert doesn't chain to a standard root
						.trustManager(new X509CertImpl(new ByteArrayInputStream(
								xdsConfigProperties.getCaCert().getBytes())))
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
