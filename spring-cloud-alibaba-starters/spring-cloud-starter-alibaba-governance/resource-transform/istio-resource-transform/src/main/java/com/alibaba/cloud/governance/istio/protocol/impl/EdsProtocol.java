package com.alibaba.cloud.governance.istio.protocol.impl;

import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsScheduledThreadPool;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;

import java.util.ArrayList;
import java.util.List;

public class EdsProtocol extends AbstractXdsProtocol<ClusterLoadAssignment> {

	public EdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool, int pollingTime) {
		super(xdsChannel, xdsScheduledThreadPool, pollingTime);
	}

	@Override
	protected List<ClusterLoadAssignment> decodeXdsResponse(DiscoveryResponse response) {
		List<ClusterLoadAssignment> endpoints = new ArrayList<>();
		for (com.google.protobuf.Any res : response.getResourcesList()) {
			try {
				ClusterLoadAssignment endpoint = res.unpack(ClusterLoadAssignment.class);
				endpoints.add(endpoint);
			}
			catch (Exception e) {
				log.error("unpack cluster failed", e);
			}
		}
		return endpoints;
	}

	@Override
	protected void clearCache() {

	}

	@Override
	public String getTypeUrl() {
		return "type.googleapis.com/envoy.config.endpoint.v3.ClusterLoadAssignment";
	}

}
