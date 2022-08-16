package com.alibaba.cloud.governance.istio;

import io.envoyproxy.envoy.service.discovery.v3.AggregatedDiscoveryServiceGrpc;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

public class XdsChannel implements AutoCloseable {
    private ManagedChannel channel;
    public XdsChannel(XdsConfigProperties xdsConfigProperties) {
        this.channel = NettyChannelBuilder.forTarget(xdsConfigProperties.getHost() + ":" + xdsConfigProperties.getPort()).negotiationType(NegotiationType.PLAINTEXT).build();
    }

    @Override
    public void close() throws Exception {
        if (channel != null) {
            channel.shutdown();
        }
    }

    public StreamObserver<DiscoveryRequest> createDiscoveryRequest(StreamObserver<DiscoveryResponse> observer) {
        return AggregatedDiscoveryServiceGrpc.newStub(channel).streamAggregatedResources(observer);
    }
}
