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

package com.alibaba.cloud.governance.istio.protocol.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.commons.governance.matcher.MatcherType;
import com.alibaba.cloud.commons.governance.matcher.StringMatcher;
import com.alibaba.cloud.router.data.controlplane.ControlPlaneConnection;
import com.alibaba.cloud.router.data.crd.LabelRouteData;
import com.alibaba.cloud.router.data.crd.MatchService;
import com.alibaba.cloud.router.data.crd.UntiedRouteDataStructure;
import com.alibaba.cloud.router.data.crd.rule.HeaderRule;
import com.alibaba.cloud.router.data.crd.rule.RouteRule;
import com.alibaba.cloud.router.data.crd.rule.UrlRule;

import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsScheduledThreadPool;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import com.alibaba.cloud.governance.istio.util.ConvUtil;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.config.route.v3.QueryParameterMatcher;
import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.config.route.v3.RouteMatch;
import io.envoyproxy.envoy.config.route.v3.VirtualHost;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;

public class RdsProtocol extends AbstractXdsProtocol<RouteConfiguration> {

	/**
	 * useless rds.
	 */
	private static final String ALLOW_ANY = "allow_any";

	private ControlPlaneConnection controlSurfaceConnection;

	public RdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool, int pollingTime,
			ControlPlaneConnection controlSurfaceConnection) {
		super(xdsChannel, xdsScheduledThreadPool, pollingTime);
		this.controlSurfaceConnection = controlSurfaceConnection;
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

	public void resolveLabelRouting(List<RouteConfiguration> routeConfigurations) {
		Map<String, UntiedRouteDataStructure> untiedRouteDataStructures = new HashMap<>();
		for (RouteConfiguration routeConfiguration : routeConfigurations) {
			List<VirtualHost> virtualHosts = routeConfiguration.getVirtualHostsList();
			for (VirtualHost virtualHost : virtualHosts) {
				UntiedRouteDataStructure untiedRouteDataStructure = new UntiedRouteDataStructure();
				String targetService = "";
				String[] serviceAndPort = virtualHost.getName().split(":");
				if (serviceAndPort.length > 0) {
					targetService = serviceAndPort[0];
				}
				if (ALLOW_ANY.equals(targetService)) {
					continue;
				}
				untiedRouteDataStructure.setTargetService(targetService);
				List<Route> routes = virtualHost.getRoutesList();
				final int n = routes.size();
				List<MatchService> matchServices = new ArrayList<>();
				LabelRouteData labelRouteData = new LabelRouteData();
				for (int i = 0; i < n; ++i) {
					Route route = routes.get(i);
					String cluster = route.getRoute().getCluster();
					String version = "";
					try {
						String[] info = cluster.split("\\|");
						version = info[2];
					}
					catch (Exception e) {
						log.error("invalid cluster info for route {}", route.getName());
					}
					// last route is default route
					if (i == n - 1) {
						labelRouteData.setDefaultRouteVersion(version);
					}
					MatchService matchService = new MatchService();
					matchService.setVersion(version);
					matchService.setRuleList(match2RouteRules(route.getMatch()));
					matchServices.add(matchService);
					untiedRouteDataStructure.setLabelRouteData(labelRouteData);
				}
				labelRouteData.setMatchRouteList(matchServices);
				untiedRouteDataStructure.setLabelRouteData(labelRouteData);
				untiedRouteDataStructures.put(untiedRouteDataStructure.getTargetService(),
						untiedRouteDataStructure);
			}
		}
		controlSurfaceConnection.getDataFromControlSurface(
				new ArrayList<>(untiedRouteDataStructures.values()));
	}

	private List<RouteRule> match2RouteRules(RouteMatch routeMatch) {
		List<RouteRule> routeRules = new ArrayList<>();
		for (HeaderMatcher headerMatcher : routeMatch.getHeadersList()) {
			HeaderRule headerRule = headerMatcher2HeaderRule(headerMatcher);
			if (headerRule != null) {
				routeRules.add(headerRule);
			}
		}

		for (QueryParameterMatcher parameterMatcher : routeMatch
				.getQueryParametersList()) {
			UrlRule.Parameter parameter = parameterMatcher2ParameterRule(
					parameterMatcher);
			if (parameter != null) {
				routeRules.add(parameter);
			}
		}

		UrlRule.Path path = new UrlRule.Path();
		switch (routeMatch.getPathSpecifierCase()) {
		case PREFIX:
			path.setCondition(MatcherType.PREFIX.toString());
			path.setValue(routeMatch.getPrefix());
			break;

		case PATH:
			path.setCondition(MatcherType.EXACT.toString());
			path.setValue(routeMatch.getPath());
			break;

		case SAFE_REGEX:
			path.setCondition(MatcherType.REGEX.toString());
			path.setValue(routeMatch.getSafeRegex().getRegex());
			break;

		default:
			// unknown type
			path = null;

		}
		if (path != null) {
			routeRules.add(path);
		}
		return routeRules;
	}

	private UrlRule.Parameter parameterMatcher2ParameterRule(
			QueryParameterMatcher queryParameterMatcher) {
		UrlRule.Parameter parameter = new UrlRule.Parameter();
		StringMatcher stringMatcher = ConvUtil
				.convStringMatcher(queryParameterMatcher.getStringMatch());
		if (stringMatcher != null) {
			parameter.setCondition(stringMatcher.getType().toString());
			parameter.setKey(queryParameterMatcher.getName());
			parameter.setValue(stringMatcher.getMatcher());
			return parameter;
		}
		return null;
	}

	private HeaderRule headerMatcher2HeaderRule(HeaderMatcher headerMatcher) {
		StringMatcher stringMatcher = ConvUtil
				.convStringMatcher(ConvUtil.headerMatch2StringMatch(headerMatcher));
		if (stringMatcher != null) {
			HeaderRule headerRule = new HeaderRule();
			headerRule.setCondition(stringMatcher.getType().toString());
			headerRule.setKey(headerMatcher.getName());
			headerRule.setValue(stringMatcher.getMatcher());
			return headerRule;
		}
		return null;
	}

	@Override
	public String getTypeUrl() {
		return "type.googleapis.com/envoy.config.route.v3.RouteConfiguration";
	}

}
