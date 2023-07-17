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

package com.alibaba.cloud.governance.istio.protocol.impl;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cloud.governance.istio.AggregateDiscoveryService;
import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.governance.istio.filter.XdsResolveFilter;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;

/**
 * RdsProtocol contains route info.
 *
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class RdsProtocol extends AbstractXdsProtocol<RouteConfiguration> {

	public RdsProtocol(XdsConfigProperties xdsConfigProperties,
			List<XdsResolveFilter<List<RouteConfiguration>>> rdsFilters,
			AggregateDiscoveryService aggregateDiscoveryService) {
		super(xdsConfigProperties, aggregateDiscoveryService);
		for (XdsResolveFilter<List<RouteConfiguration>> filter : rdsFilters) {
			if (IstioConstants.RDS_URL.equals(filter.getTypeUrl())) {
				filters.add(filter);
			}
		}
	}

	@Override
	public List<RouteConfiguration> decodeXdsResponse(DiscoveryResponse response) {
		List<RouteConfiguration> routes = new ArrayList<>();
		for (com.google.protobuf.Any res : response.getResourcesList()) {
			try {
				RouteConfiguration route = res.unpack(RouteConfiguration.class);
				routes.add(route);
			}
			catch (Exception e) {
				log.error("Unpack cluster failed", e);
			}
		}
		return routes;
	}

	@Override
	public String getTypeUrl() {
		return IstioConstants.RDS_URL;
	}

	@Override
	public void onResponseDecoded(List<RouteConfiguration> resources) {
		if (log.isDebugEnabled()) {
			log.debug("A Xds configuration update is finished");
		}
		fireXdsFilters(resources);
	}

}
