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

import com.alibaba.cloud.commons.governance.event.LabelRoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.labelrouting.crd.LabelRouteRule;
import com.alibaba.cloud.commons.governance.labelrouting.crd.MatchService;
import com.alibaba.cloud.commons.governance.labelrouting.crd.UntiedRouteDataStructure;
import com.alibaba.cloud.commons.governance.labelrouting.crd.rule.HeaderRule;
import com.alibaba.cloud.commons.governance.labelrouting.crd.rule.RouteRule;
import com.alibaba.cloud.commons.governance.labelrouting.crd.rule.UrlRule;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.commons.matcher.StringMatcher;
import com.alibaba.cloud.commons.matcher.StringMatcherType;
import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.XdsScheduledThreadPool;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import com.alibaba.cloud.governance.istio.util.ConvUtil;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.config.route.v3.QueryParameterMatcher;
import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.config.route.v3.RouteMatch;
import io.envoyproxy.envoy.config.route.v3.VirtualHost;
import io.envoyproxy.envoy.config.route.v3.WeightedCluster;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 */
public class RdsProtocol extends AbstractXdsProtocol<RouteConfiguration> {

	/**
	 * useless rds.
	 */
	private static final String ALLOW_ANY = "allow_any";

	private static final String HEADER = "header";

	private static final String PARAMETER = "parameter";

	private static final String PATH = "path";

	public RdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool,
			XdsConfigProperties xdsConfigProperties) {
		super(xdsChannel, xdsScheduledThreadPool, xdsConfigProperties);
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
				log.error("unpack cluster failed", e);
			}
		}
		return routes;
	}

	public void resolveLabelRouting(List<RouteConfiguration> routeConfigurations) {
		if (routeConfigurations == null) {
			return;
		}
		Map<String, UntiedRouteDataStructure> untiedRouteDataStructures = new HashMap<>();
		for (RouteConfiguration routeConfiguration : routeConfigurations) {
			List<VirtualHost> virtualHosts = routeConfiguration.getVirtualHostsList();
			for (VirtualHost virtualHost : virtualHosts) {
				UntiedRouteDataStructure untiedRouteDataStructure = new UntiedRouteDataStructure();
				String targetService = "";
				String[] serviceAndPort = virtualHost.getName().split(":");
				if (serviceAndPort.length > 0) {
					targetService = serviceAndPort[0].split("\\.")[0];
				}
				if (ALLOW_ANY.equals(targetService)) {
					continue;
				}
				untiedRouteDataStructure.setTargetService(targetService);
				List<Route> routes = virtualHost.getRoutesList();
				LabelRouteRule labelRouteRule = getLabelRouteData(routes);
				untiedRouteDataStructure.setLabelRouteRule(labelRouteRule);
				untiedRouteDataStructures.put(untiedRouteDataStructure.getTargetService(),
						untiedRouteDataStructure);
			}
		}
		applicationContext.publishEvent(
				new LabelRoutingDataChangedEvent(untiedRouteDataStructures.values()));
	}

	private LabelRouteRule getLabelRouteData(List<Route> routes) {
		List<MatchService> matchServices = new ArrayList<>();
		LabelRouteRule labelRouteRule = new LabelRouteRule();
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
			log.error("invalid cluster info for route {}", route.getName());
		}
		MatchService matchService = new MatchService();
		matchService.setVersion(version);
		matchService.setRuleList(match2RouteRules(route.getMatch()));
		matchService.setWeight(weight);
		return matchService;
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
		path.setType(PATH);
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

	private UrlRule.Parameter parameterMatcher2ParameterRule(
			QueryParameterMatcher queryParameterMatcher) {
		UrlRule.Parameter parameter = new UrlRule.Parameter();
		StringMatcher stringMatcher = ConvUtil
				.convStringMatcher(queryParameterMatcher.getStringMatch());
		if (stringMatcher != null) {
			parameter.setCondition(stringMatcher.getType().toString());
			parameter.setKey(queryParameterMatcher.getName());
			parameter.setValue(stringMatcher.getMatcher());
			parameter.setType(PARAMETER);
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
			headerRule.setType(HEADER);
			return headerRule;
		}
		return null;
	}

	@Override
	public String getTypeUrl() {
		return IstioConstants.RDS_URL;
	}

}
