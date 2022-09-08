package com.alibaba.cloud.governance.istio.protocol.impl;

import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsScheduledThreadPool;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;

import java.util.ArrayList;
import java.util.List;

public class RdsProtocol extends AbstractXdsProtocol<RouteConfiguration> {

	public RdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool, int pollingTime) {
		super(xdsChannel, xdsScheduledThreadPool, pollingTime);
	}

	@Override
	protected List<RouteConfiguration> decodeXdsResponse(DiscoveryResponse response) {
		List<RouteConfiguration> routes = new ArrayList<>();
		for (com.google.protobuf.Any res : response.getResourcesList()) {
			try {
				RouteConfiguration route = res.unpack(RouteConfiguration.class);
				routes.add(route);
			}
			catch (Exception e) {
				log.error("unpack cluster failed", e);
			}
		}
		return routes;
	}

	@Override
	protected void clearCache() {

	}

	@Override
	public String getTypeUrl() {
		return "type.googleapis.com/envoy.config.route.v3.RouteConfiguration";
	}

}
