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

package com.alibaba.cloud.governance.istio.filter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.commons.governance.event.RoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.routing.MatchService;
import com.alibaba.cloud.commons.governance.routing.RoutingRule;
import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import com.alibaba.cloud.commons.governance.routing.rule.HeaderRoutingRule;
import com.alibaba.cloud.commons.governance.routing.rule.Rule;
import com.alibaba.cloud.commons.governance.routing.rule.UrlRoutingRule;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.commons.matcher.StringMatcherType;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.governance.istio.filter.AbstractXdsResolveFilter;
import com.alibaba.cloud.governance.istio.util.ConvUtil;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.config.route.v3.QueryParameterMatcher;
import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.config.route.v3.RouteMatch;
import io.envoyproxy.envoy.config.route.v3.VirtualHost;
import io.envoyproxy.envoy.config.route.v3.WeightedCluster;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class RoutingXdsResolveFilter
		extends AbstractXdsResolveFilter<List<RouteConfiguration>> {

	@Override
	public boolean resolve(List<RouteConfiguration> routeConfigurations) {
		if (routeConfigurations == null) {
			return false;
		}
		Map<String, UnifiedRoutingDataStructure> untiedRouteDataStructures = new HashMap<>();
		for (RouteConfiguration routeConfiguration : routeConfigurations) {
			List<VirtualHost> virtualHosts = routeConfiguration.getVirtualHostsList();
			for (VirtualHost virtualHost : virtualHosts) {
				UnifiedRoutingDataStructure unifiedRouteDataStructure = new UnifiedRoutingDataStructure();
				String targetService = "";
				String[] serviceAndPort = virtualHost.getName().split(":");
				if (serviceAndPort.length > 0) {
					targetService = serviceAndPort[0].split("\\.")[0];
				}
				if (ALLOW_ANY.equals(targetService)) {
					continue;
				}
				unifiedRouteDataStructure.setTargetService(targetService);
				List<Route> routes = virtualHost.getRoutesList();
				RoutingRule labelRouteRule = getRouteData(routes);
				unifiedRouteDataStructure.setLabelRouteRule(labelRouteRule);
				untiedRouteDataStructures.put(
						unifiedRouteDataStructure.getTargetService(),
						unifiedRouteDataStructure);
			}
		}
		applicationContext.publishEvent(
				new RoutingDataChangedEvent(this, untiedRouteDataStructures.values()));
		return true;
	}

	private RoutingRule getRouteData(List<Route> routes) {
		List<MatchService> matchServices = new ArrayList<>();
		RoutingRule labelRouteRule = new RoutingRule();
		for (Route route : routes) {
			String cluster = route.getRoute().getCluster();
			if (StringUtils.isNotEmpty(cluster)) {
				MatchService matchService = getMatchService(route, cluster, 100);
				matchServices.add(matchService);
			}
			WeightedCluster weightedCluster = route.getRoute().getWeightedClusters();
			for (WeightedCluster.ClusterWeight clusterWeight : weightedCluster
					.getClustersList()) {
				MatchService matchService = getMatchService(route,
						clusterWeight.getName(), clusterWeight.getWeight().getValue());
				matchServices.add(matchService);
			}
		}
		labelRouteRule.setMatchRouteList(matchServices);
		if (!matchServices.isEmpty()) {
			labelRouteRule.setDefaultRouteVersion(
					matchServices.get(matchServices.size() - 1).getVersion());
		}
		return labelRouteRule;
	}

	private MatchService getMatchService(Route route, String cluster, int weight) {
		String version = "";
		try {
			String[] info = cluster.split("\\|");
			version = info[2];
		}
		catch (Exception e) {
			log.error("Invalid cluster info for route {}", route.getName());
		}
		MatchService matchService = new MatchService();
		matchService.setVersion(version);
		matchService.setRuleList(match2RouteRules(route.getMatch()));
		matchService.setWeight(weight);
		return matchService;
	}

	private List<Rule> match2RouteRules(RouteMatch routeMatch) {
		List<Rule> routeRules = new ArrayList<>();
		for (HeaderMatcher headerMatcher : routeMatch.getHeadersList()) {
			HeaderRoutingRule headerRule = ConvUtil
					.headerMatcher2HeaderRule(headerMatcher);
			if (headerRule != null) {
				routeRules.add(headerRule);
			}
		}

		for (QueryParameterMatcher parameterMatcher : routeMatch
				.getQueryParametersList()) {
			UrlRoutingRule.ParameterRoutingRule parameterRoutingRule = ConvUtil
					.parameterMatcher2ParameterRule(parameterMatcher);
			if (parameterRoutingRule != null) {
				routeRules.add(parameterRoutingRule);
			}
		}

		UrlRoutingRule.PathRoutingRule path = new UrlRoutingRule.PathRoutingRule();
		switch (routeMatch.getPathSpecifierCase()) {
		case PREFIX:
			path.setCondition(StringMatcherType.PREFIX.toString());
			path.setValue(routeMatch.getPrefix());
			break;

		case PATH:
			path.setCondition(StringMatcherType.EXACT.toString());
			path.setValue(routeMatch.getPath());
			break;

		case SAFE_REGEX:
			path.setCondition(StringMatcherType.REGEX.toString());
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

	@Override
	public String getTypeUrl() {
		return IstioConstants.RDS_URL;
	}

}
