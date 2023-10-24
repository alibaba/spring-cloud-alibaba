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

package com.alibaba.cloud.routing.ribbon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.commons.governance.routing.MatchService;
import com.alibaba.cloud.commons.governance.routing.rule.Rule;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.cloud.routing.constant.LabelRoutingConstants;
import com.alibaba.cloud.routing.context.LabelRoutingContextHolder;
import com.alibaba.cloud.routing.properties.LabelRoutingProperties;
import com.alibaba.cloud.routing.publish.TargetServiceChangedPublisher;
import com.alibaba.cloud.routing.repository.RoutingDataRepository;
import com.alibaba.cloud.routing.util.ConditionMatchUtil;
import com.alibaba.cloud.routing.util.LoadBalanceUtil;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.PredicateBasedRule;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author HH
 * @since 2.2.10-RC1
 */
public class RoutingLoadBalanceRule extends PredicateBasedRule {

	private static final Logger LOG = LoggerFactory
			.getLogger(RoutingLoadBalanceRule.class);

	/**
	 * Composite route. todo
	 */
	private AbstractServerPredicate predicate;

	@Resource
	private LabelRoutingContextHolder labelRoutingContextHolder;

	@Autowired
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	@Autowired
	private NacosServiceManager nacosServiceManager;

	@Autowired
	private RoutingDataRepository routingDataRepository;

	@Autowired
	private LabelRoutingProperties labelRoutingProperties;

	@Autowired
	private TargetServiceChangedPublisher targetServiceChangedPublisher;

	DynamicServerListLoadBalancer loadBalancer = null;

	@Value("${" + LabelRoutingConstants.ZONE_AVOIDANCE_RULE_ENABLED + ":true}")
	private boolean zoneAvoidanceRuleEnabled;

	@Override
	public Server choose(Object key) {

		try {
			loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
			String targetServiceName = getTargetServiceName();
			targetServiceChangedPublisher.addTargetService(targetServiceName);

			// If routeData isn't present, use normal load balance rule.
			final HashMap<String, List<MatchService>> routeData = routingDataRepository
					.getRouteRule(targetServiceName);

			if (routeData == null) {

				return LoadBalanceUtil.loadBalanceByOrdinaryRule(loadBalancer, key,
						labelRoutingProperties.getRule());
			}

			List<Instance> instances = getInstanceFromNacos(targetServiceName);

			// Filter by route rules,the result will be kept in versionSet and weightMap.
			HashSet<String> versionSet = new HashSet<>();
			HashMap<String, Integer> weightMap = new HashMap<>();
			HashSet<String> fallbackVersionSet = new HashSet<>();
			HashMap<String, Integer> fallbackWeightMap = new HashMap<>();

			serviceFilterStrategy(targetServiceName, versionSet, weightMap,
					fallbackVersionSet, fallbackWeightMap);

			// Filter instances by versionSet and weightMap.
			double[] weightArray = new double[instances.size()];
			HashMap<String, List<Instance>> instanceMap = new HashMap<>();
			for (Instance instance : instances) {
				String version = instance.getMetadata()
						.get(LabelRoutingConstants.VERSION);
				if (versionSet.contains(version)) {
					List<Instance> instanceList = instanceMap.get(version);
					if (instanceList == null) {
						instanceList = new ArrayList<>();
					}
					instanceList.add(instance);
					instanceMap.put(version, instanceList);
				}
			}

			// None instance match rule
			if (CollectionUtils.isEmpty(instanceMap)) {
				LOG.warn("no instance match route rule");
				for (Instance instance : instances) {
					String version = instance.getMetadata()
							.get(LabelRoutingConstants.VERSION);
					if (fallbackVersionSet.contains(version)) {
						List<Instance> instanceList = instanceMap.get(version);
						if (instanceList == null) {
							instanceList = new ArrayList<>();
						}
						instanceList.add(instance);
						instanceMap.put(version, instanceList);
					}
				}

				return chooseServerByWeight(instanceMap, fallbackWeightMap, weightArray);
			}

			// Routing with Weight algorithm.
			return chooseServerByWeight(instanceMap, weightMap, weightArray);

		}
		catch (Exception e) {
			LOG.warn("LabelRouteRule error", e);
			return null;
		}
	}

	/**
	 * Get targetServiceName.
	 * @return String.
	 */
	protected String getTargetServiceName() {
		return loadBalancer.getName();
	}

	/**
	 * Get instances from register-center.
	 * @param targetServiceName target service.
	 * @return Instance list.
	 */
	protected List<Instance> getInstanceFromNacos(String targetServiceName) {

		List<Instance> instances = null;
		String group = this.nacosDiscoveryProperties.getGroup();
		final NamingService namingService = nacosServiceManager.getNamingService();

		try {
			instances = namingService.selectInstances(targetServiceName, group, true);
		}
		catch (NacosException e) {
			LOG.warn("no instance in service {} ", targetServiceName);
		}

		if (CollectionUtils.isEmpty(instances)) {
			LOG.warn("no instance in service {} ", targetServiceName);
			return null;
		}

		return instances;
	}

	@Override
	public AbstractServerPredicate getPredicate() {

		return this.predicate;
	}

	private HashMap<String, String> getHeaderNames(ServerHttpRequest request) {

		HttpHeaders headers = request.getHeaders();
		Map<String, String> map = headers.toSingleValueMap();

		HashMap<String, String> resMap = new HashMap<>();

		Set<String> strings = map.keySet();
		for (String string : strings) {
			resMap.put(string, map.get(string));
		}

		return resMap;
	}

	private String getRequestURI(ServerHttpRequest request) {

		return request.getPath().toString();
	}

	private Map<String, String> getHeaderNames(HttpServletRequest request) {

		final Enumeration<String> headerNames = request.getHeaderNames();
		HashMap<String, String> requestHeaders = new HashMap<>();

		if (headerNames != null) {
			while (headerNames.hasMoreElements()) {
				String name = headerNames.nextElement();
				String value = request.getHeader(name);
				requestHeaders.put(name, value);
			}
		}

		return requestHeaders;
	}

	private void serviceFilterStrategy(String targetServiceName,
			HashSet<String> versionSet, HashMap<String, Integer> weightMap,
			HashSet<String> fallbackVersionSet,
			HashMap<String, Integer> fallbackWeightMap) {

		if (Objects.nonNull(RequestContextHolder.getRequestAttributes())) {
			final HttpServletRequest request = ((ServletRequestAttributes) Objects
					.requireNonNull(RequestContextHolder.getRequestAttributes()))
							.getRequest();
			if (Objects.nonNull(request)) {
				serviceFilter(targetServiceName, versionSet, weightMap,
						fallbackVersionSet, fallbackWeightMap, request);
			}
		}
		else {
			ServerHttpRequest serverHttpRequest = labelRoutingContextHolder
					.getServerHttpRequest();
			serviceFilter(targetServiceName, versionSet, weightMap, fallbackVersionSet,
					fallbackWeightMap, serverHttpRequest);
		}
	}

	/**
	 * For ServerHttpRequest.
	 * @param targetServiceName target service
	 * @param versionSet version
	 * @param weightMap weight
	 * @param fallbackVersionSet fallback version
	 * @param fallbackWeightMap fallback weight
	 * @param request ServerHttpRequest Object
	 */
	private void serviceFilter(String targetServiceName, HashSet<String> versionSet,
			HashMap<String, Integer> weightMap, HashSet<String> fallbackVersionSet,
			HashMap<String, Integer> fallbackWeightMap, ServerHttpRequest request) {

		// Get request metadata.
		HashMap<String, String> requestHeaders = getHeaderNames(request);

		Map<String, String> parameterMap = request.getQueryParams().toSingleValueMap();

		Map<String, String[]> buildParameterMap = new HashMap<>();

		Set<String> strings = parameterMap.keySet();
		for (String string : strings) {
			buildParameterMap.put(string, new String[] { parameterMap.get(string) });
		}

		int defaultVersionWeight = RoutingDataRepository.SUM_WEIGHT;
		boolean isMatch = false;

		// Parse rule.
		if (requestHeaders.size() > 0) {
			for (String keyName : requestHeaders.keySet()) {
				int weight = matchRule(targetServiceName, keyName, requestHeaders,
						buildParameterMap, getRequestURI(request), versionSet, weightMap,
						fallbackVersionSet, fallbackWeightMap);
				if (weight != LabelRoutingConstants.NO_MATCH) {
					isMatch = true;
					defaultVersionWeight -= weight;
					break;
				}
			}
		}

		if (!isMatch && parameterMap != null) {
			for (String keyName : parameterMap.keySet()) {
				int weight = matchRule(targetServiceName, keyName, requestHeaders,
						buildParameterMap, getRequestURI(request), versionSet, weightMap,
						fallbackVersionSet, fallbackWeightMap);
				if (weight != LabelRoutingConstants.NO_MATCH) {
					isMatch = true;
					defaultVersionWeight -= weight;
					break;
				}
			}
		}

		final List<MatchService> pathRules = routingDataRepository
				.getPathRules(targetServiceName);
		if (!isMatch && pathRules != null) {
			for (MatchService matchService : pathRules) {
				if (matchService.getRuleList().get(0).getValue()
						.equals(getRequestURI(request))) {
					String version = matchService.getVersion();
					Integer weight = matchService.getWeight();
					versionSet.add(version);
					weightMap.put(version, weight);
					defaultVersionWeight -= weight;
				}
			}
		}

		// Add default route
		if (defaultVersionWeight > RoutingDataRepository.MIN_WEIGHT) {
			String defaultRouteVersion = routingDataRepository
					.getDefaultRouteVersion(targetServiceName);
			versionSet.add(defaultRouteVersion);
			weightMap.put(defaultRouteVersion, defaultVersionWeight);
		}

	}

	/**
	 * For HttpServletRequest.
	 * @param targetServiceName target service
	 * @param versionSet version
	 * @param weightMap weight
	 * @param fallbackVersionSet fallback version
	 * @param fallbackWeightMap fallback weight
	 * @param request httpServletRequest Object
	 */
	private void serviceFilter(String targetServiceName, HashSet<String> versionSet,
			HashMap<String, Integer> weightMap, HashSet<String> fallbackVersionSet,
			HashMap<String, Integer> fallbackWeightMap, HttpServletRequest request) {

		// Get request metadata.
		HashMap<String, String> requestHeaders = (HashMap<String, String>) getHeaderNames(
				request);

		final Map<String, String[]> parameterMap = request.getParameterMap();
		int defaultVersionWeight = RoutingDataRepository.SUM_WEIGHT;
		boolean isMatch = false;

		// Parse rule.
		if (requestHeaders.size() > 0) {
			for (String keyName : requestHeaders.keySet()) {
				int weight = matchRule(targetServiceName, keyName, requestHeaders,
						parameterMap, request.getRequestURI(), versionSet, weightMap,
						fallbackVersionSet, fallbackWeightMap);
				if (weight != LabelRoutingConstants.NO_MATCH) {
					isMatch = true;
					defaultVersionWeight -= weight;
					break;
				}
			}
		}

		if (!isMatch && parameterMap != null) {
			for (String keyName : parameterMap.keySet()) {
				int weight = matchRule(targetServiceName, keyName, requestHeaders,
						parameterMap, request.getRequestURI(), versionSet, weightMap,
						fallbackVersionSet, fallbackWeightMap);
				if (weight != LabelRoutingConstants.NO_MATCH) {
					isMatch = true;
					defaultVersionWeight -= weight;
					break;
				}
			}
		}

		final List<MatchService> pathRules = routingDataRepository
				.getPathRules(targetServiceName);
		if (!isMatch && pathRules != null) {
			for (MatchService matchService : pathRules) {
				if (matchService.getRuleList().get(0).getValue()
						.equals(request.getRequestURI())) {
					String version = matchService.getVersion();
					Integer weight = matchService.getWeight();
					versionSet.add(version);
					weightMap.put(version, weight);
					defaultVersionWeight -= weight;
				}
			}
		}

		// Add default route
		if (defaultVersionWeight > RoutingDataRepository.MIN_WEIGHT) {
			String defaultRouteVersion = routingDataRepository
					.getDefaultRouteVersion(targetServiceName);
			versionSet.add(defaultRouteVersion);
			weightMap.put(defaultRouteVersion, defaultVersionWeight);
		}

	}

	private int matchRule(String targetServiceName, String keyName,
			final HashMap<String, String> requestHeaders,
			final Map<String, String[]> parameterMap, final String uri,
			HashSet<String> versionSet, HashMap<String, Integer> weightMap,
			HashSet<String> fallbackVersionSet,
			HashMap<String, Integer> fallbackWeightMap) {
		final List<MatchService> matchServiceList = routingDataRepository
				.getRouteRule(targetServiceName).get(keyName);
		if (matchServiceList == null) {
			return LabelRoutingConstants.NO_MATCH;
		}
		for (MatchService matchService : matchServiceList) {
			final List<Rule> ruleList = matchService.getRuleList();
			String version = matchService.getVersion();
			Integer weight = matchService.getWeight();
			String fallback = matchService.getFallback();
			boolean isMatchRule = true;
			for (Rule routeRule : ruleList) {
				String type = routeRule.getType();
				switch (type) {
				case LabelRoutingConstants.PATH:
					isMatchRule = parseRequestPath(routeRule, uri);
					break;
				case LabelRoutingConstants.HEADER:
					isMatchRule = parseRequestHeader(routeRule, requestHeaders);
					break;
				case LabelRoutingConstants.PARAMETER:
					isMatchRule = parseRequestParameter(routeRule, parameterMap);
					break;
				default:
					throw new UnsupportedOperationException(
							"unsupported string compare operation");
				}
				if (!isMatchRule) {
					break;
				}
			}
			if (!isMatchRule) {
				continue;
			}
			versionSet.add(version);
			fallbackVersionSet.add(fallback);
			fallbackWeightMap.put(fallback, weight);
			weightMap.put(version, weight);
			return weight;
		}
		return LabelRoutingConstants.NO_MATCH;
	}

	private boolean parseRequestPath(final Rule routeRule, final String uri) {
		String condition = routeRule.getCondition();
		String value = routeRule.getValue();

		return conditionMatch(condition, value, uri);
	}

	private boolean parseRequestHeader(final Rule routeRule,
			final HashMap<String, String> requestHeaders) {
		if (requestHeaders.size() == 0) {
			return false;
		}
		String condition = routeRule.getCondition();
		String value = routeRule.getValue();
		String headerValue = requestHeaders.get(routeRule.getKey());
		return conditionMatch(condition, value, headerValue);
	}

	private boolean parseRequestParameter(final Rule routeRule,
			final Map<String, String[]> parameterMap) {
		if (parameterMap == null || parameterMap.size() == 0) {
			return false;
		}
		String condition = routeRule.getCondition();
		String value = routeRule.getValue();
		String[] paramValues = parameterMap.get(routeRule.getKey());
		String paramValue = paramValues == null ? null : paramValues[0];
		return conditionMatch(condition, value, paramValue);
	}

	private boolean conditionMatch(String condition, String str, String comparator) {
		switch (condition) {
		case ConditionMatchUtil.EXACT:
		case ConditionMatchUtil.EQUAL:
			return ConditionMatchUtil.exactMatch(str, comparator);
		case ConditionMatchUtil.REGEX:
			return ConditionMatchUtil.regexMatch(str, comparator);
		case ConditionMatchUtil.PREFIX:
			return ConditionMatchUtil.prefixMatch(str, comparator);
		case ConditionMatchUtil.CONTAIN:
			return ConditionMatchUtil.containMatch(str, comparator);
		case ConditionMatchUtil.GREATER:
			return ConditionMatchUtil.greaterMatch(str, comparator);
		case ConditionMatchUtil.LESS:
			return ConditionMatchUtil.lessMatch(str, comparator);
		case ConditionMatchUtil.NOT_EQUAL:
			return ConditionMatchUtil.noEqualMatch(str, comparator);
		default:
			throw new RuntimeException("unsupported string compare operation");
		}
	}

	private Server chooseServerByWeight(HashMap<String, List<Instance>> instanceMap,
			HashMap<String, Integer> weightMap, double[] weightArray) {

		if (zoneAvoidanceRuleEnabled) {

			instanceMap = chooseServerByRegionalAffinity(instanceMap);
		}

		int index = 0;
		double sum = 0.0D;
		List<Instance> instances = new ArrayList<>();

		for (String version : instanceMap.keySet()) {
			int weight = weightMap.get(version);
			List<Instance> instanceList = instanceMap.get(version);
			for (Instance instance : instanceList) {
				instances.add(instance);

				weightArray[index] = LabelRoutingConstants.KEEP_ACCURACY * weight
						/ instanceList.size() + sum;
				sum = weightArray[index];

				index++;
			}
		}

		if (instances.size() == 1) {
			return new NacosServer(instances.get(0));
		}

		if (sum > RoutingDataRepository.SUM_WEIGHT) {
			LOG.error("Sum of weight has over {} ", RoutingDataRepository.SUM_WEIGHT);
		}

		double random = ThreadLocalRandom.current().nextDouble(
				RoutingDataRepository.MIN_WEIGHT, RoutingDataRepository.SUM_WEIGHT);

		int chooseServiceIndex = Arrays.binarySearch(weightArray, random);
		if (chooseServiceIndex < 0) {
			chooseServiceIndex = -chooseServiceIndex - 1;
		}

		return new NacosServer(instances.get(chooseServiceIndex));
	}

	private HashMap<String, List<Instance>> chooseServerByRegionalAffinity(
			HashMap<String, List<Instance>> map) {

		Map<String, String> regionalAffinityLabels = getRegionalAffinityLabels();

		if (CollectionUtils.isEmpty(regionalAffinityLabels)) {

			return map;
		}

		String region = getRegionalAffinityLabels().get(LabelRoutingConstants.REGION);
		String zone = getRegionalAffinityLabels().get(LabelRoutingConstants.ZONE);

		if (StringUtils.isEmpty(region) && StringUtils.isEmpty(zone)) {

			return map;
		}

		HashMap<String, List<Instance>> serverMap = new HashMap<>();
		List<Instance> serverList = new ArrayList<>();
		for (String version : map.keySet()) {
			List<Instance> instances = map.get(version);
			for (Instance instance : instances) {
				Map<String, String> metadata = instance.getMetadata();
				String serverRegion = metadata.get(LabelRoutingConstants.REGION);
				String serverZone = metadata.get(LabelRoutingConstants.ZONE);
				if (StringUtils.isNotEmpty(serverRegion)
						&& StringUtils.isNotEmpty(serverZone)) {
					if (Objects.equals(region, serverRegion)) {
						if (Objects.equals(zone, serverZone)) {
							serverList.add(instance);
						}
					}
				}
			}
			serverMap.put(version, serverList);
		}

		// If there is no suitable instance, route the whole service instance
		if (checkMap(serverMap)) {

			LOG.warn(
					"The Region Affinity Route Label Selection Service instance is empty.");
			return map;
		}

		return new HashMap<>(serverMap);
	}

	private Map<String, String> getRegionalAffinityLabels() {

		Map<String, String> resMap = new ConcurrentHashMap<>();

		// Get the regional affinity route label from the service
		String region = labelRoutingContextHolder.getLabelRouteRegion();
		String zone = labelRoutingContextHolder.getLabelRouteZone();

		if (StringUtils.isEmpty(region) && StringUtils.isEmpty(zone)) {

			LOG.warn("Regional affinity labels is null.");
			return null;
		}

		resMap.put(LabelRoutingConstants.REGION, region);
		resMap.put(LabelRoutingConstants.ZONE, zone);

		return resMap;
	}

	private static boolean checkMap(Map<String, List<Instance>> map) {

		if (CollectionUtils.isEmpty(map) || CollectionUtils.isEmpty(map.values())) {
			return true;
		}

		Set<String> values = map.keySet();
		for (String value : values) {
			List<Instance> instances = map.get(value);
			if (CollectionUtils.isEmpty(instances)) {
				return true;
			}
			for (Instance instance : instances) {
				if (Objects.isNull(instance)) {
					return true;
				}
			}
		}

		return false;
	}

}
