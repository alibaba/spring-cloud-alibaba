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

package com.alibaba.cloud.governance.opensergo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.commons.governance.labelrouting.LabelRouteRule;
import com.alibaba.cloud.commons.governance.labelrouting.MatchService;
import com.alibaba.cloud.commons.governance.labelrouting.UnifiedRouteDataStructure;
import com.alibaba.cloud.commons.governance.labelrouting.rule.HeaderRule;
import com.alibaba.cloud.commons.governance.labelrouting.rule.RouteRule;
import com.alibaba.cloud.commons.governance.labelrouting.rule.UrlRule;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.commons.matcher.StringMatcher;
import com.alibaba.cloud.commons.matcher.StringMatcherType;
import com.alibaba.cloud.governance.opensergo.util.ConvUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.envoy.config.route.v3.ClusterSpecifierPlugin;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.config.route.v3.QueryParameterMatcher;
import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.config.route.v3.RouteMatch;
import io.envoyproxy.envoy.config.route.v3.VirtualHost;
import io.envoyproxy.envoy.config.route.v3.WeightedCluster;
import io.opensergo.proto.router.v1.ClusterFallbackConfig_ClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author panxiaojun233
 * @author <a href="m13201628570@163.com"></a>
 */
public class OpenSergoTrafficRouterParser {

	protected static final Logger log = LoggerFactory
			.getLogger(OpenSergoTrafficRouterParser.class);

	private static final String HEADER = "header";

	private static final String PARAMETER = "parameter";

	private static final String PATH = "path";

	public OpenSergoTrafficRouterParser() {
	}

	/**
	 * transform rds RouterConfig list to spring cloud alibaba router data list.
	 * @param routeConfigurations the routerConfig list from OpenSergo control plane.
	 * @return spring cloud alibaba router rules.
	 * @throws InvalidProtocolBufferException transform exception.
	 */
	public Collection<UnifiedRouteDataStructure> resolveLabelRouting(
			List<RouteConfiguration> routeConfigurations)
			throws InvalidProtocolBufferException {
		if (routeConfigurations == null) {
			return new ArrayList<>();
		}
		Map<String, UnifiedRouteDataStructure> unifiedRouteDataStructures = new HashMap<>();
		for (RouteConfiguration routeConfiguration : routeConfigurations) {
			List<VirtualHost> virtualHosts = routeConfiguration.getVirtualHostsList();
			for (VirtualHost virtualHost : virtualHosts) {
				UnifiedRouteDataStructure unifiedRouteDataStructure = new UnifiedRouteDataStructure();
				String targetService = "";
				String[] serviceAndPort = virtualHost.getName().split(":");
				if (serviceAndPort.length > 0) {
					targetService = serviceAndPort[0].split("\\.")[0];
				}
				unifiedRouteDataStructure.setTargetService(targetService);
				List<Route> routes = virtualHost.getRoutesList();
				LabelRouteRule labelRouteRule = getLabelRouteData(routes);
				unifiedRouteDataStructure.setLabelRouteRule(labelRouteRule);
				unifiedRouteDataStructures.put(
						unifiedRouteDataStructure.getTargetService(),
						unifiedRouteDataStructure);
			}
		}
		return unifiedRouteDataStructures.values();
	}

	private LabelRouteRule getLabelRouteData(List<Route> routes)
			throws InvalidProtocolBufferException {
		List<MatchService> matchServices = new ArrayList<>();
		LabelRouteRule labelRouteRule = new LabelRouteRule();
		for (Route route : routes) {
			ClusterSpecifierPlugin clusterSpecifierPlugin = route.getRoute()
					.getInlineClusterSpecifierPlugin();
			String cluster = "";
			String fallbackCluster = "";
			if (clusterSpecifierPlugin != null) {
				ClusterFallbackConfig_ClusterConfig fallbackConfig = ConvUtils
						.convFallbackClusterConfig(clusterSpecifierPlugin);
				fallbackCluster = fallbackConfig.getFallbackCluster();
				cluster = fallbackConfig.getRoutingCluster();
			}
			if (StringUtils.isEmpty(cluster)) {
				cluster = route.getRoute().getCluster();
			}

			if (StringUtils.isNotEmpty(cluster)) {
				MatchService matchService = null;
				if (StringUtils.isNotEmpty(fallbackCluster)) {
					matchService = getMatchService(route, cluster, 100,
							getVersion(route, fallbackCluster));
				}
				else {
					matchService = getMatchService(route, cluster, 100);
				}

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
		return getMatchService(route, cluster, weight, null);
	}

	private MatchService getMatchService(Route route, String cluster, int weight,
			String fallback) {
		String version = getVersion(route, cluster);
		MatchService matchService = new MatchService();
		matchService.setVersion(version);
		matchService.setRuleList(match2RouteRules(route.getMatch()));
		matchService.setWeight(weight);
		if (StringUtils.isNotEmpty(fallback)) {
			matchService.setFallback(fallback);
		}
		return matchService;
	}

	private String getVersion(Route route, String cluster) {
		String version = "";
		try {
			String[] info = cluster.split("\\|");
			version = info[2];
		}
		catch (Exception e) {
			log.error("invalid cluster info for route {}", route.getName());
		}
		return version;
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
		StringMatcher stringMatcher = ConvUtils
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
		StringMatcher stringMatcher = ConvUtils
				.convStringMatcher(ConvUtils.headerMatch2StringMatch(headerMatcher));
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

}
