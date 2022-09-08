package com.alibaba.cloud.governance.istio.protocol.impl;

import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsScheduledThreadPool;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CdsProtocol extends AbstractXdsProtocol<Cluster> {

	public CdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool, int pollingTime) {
		super(xdsChannel, xdsScheduledThreadPool, pollingTime);
	}

	@Override
	protected List<Cluster> decodeXdsResponse(DiscoveryResponse response) {
		List<Cluster> clusters = new ArrayList<>();
		for (com.google.protobuf.Any res : response.getResourcesList()) {
			try {
				Cluster cluster = res.unpack(Cluster.class);
				clusters.add(cluster);
			}
			catch (Exception e) {
				log.error("unpack cluster failed", e);
			}
		}
		return clusters;
	}

	@Override
	protected void clearCache() {

	}

	public Set<String> getEndPointNames(List<Cluster> clusters) {
		Set<String> endpoints = new HashSet<>();
		for (Cluster cluster : clusters) {
			cluster.getEdsClusterConfig().getServiceName();
			endpoints.add(cluster.getEdsClusterConfig().getServiceName());
		}
		return endpoints;
	}

	@Override
	public String getTypeUrl() {
		return "type.googleapis.com/envoy.config.cluster.v3.Cluster";
	}

}
