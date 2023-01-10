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

package com.alibaba.cloud.router.ribbon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.commons.governance.labelrouting.MatchService;
import com.alibaba.cloud.commons.governance.labelrouting.rule.RouteRule;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.cloud.router.RouterProperties;
import com.alibaba.cloud.router.publish.TargetServiceChangedPublisher;
import com.alibaba.cloud.router.repository.RouteDataRepository;
import com.alibaba.cloud.router.util.ConditionMatchUtil;
import com.alibaba.cloud.router.util.LoadBalanceUtil;
import com.alibaba.cloud.router.util.RequestContext;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.PredicateBasedRule;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author HH
 */
public class LabelRouteLBRule extends PredicateBasedRule {

	private static final Logger LOG = LoggerFactory.getLogger(LabelRouteLBRule.class);

	/**
	 * Support Parsing Rules from path,only URI at present.
	 */
	private static final String PATH = "path";

	/**
	 * Support Parsing Rules from header.
	 */
	private static final String HEADER = "header";

	/**
	 * Support Parsing Rules from parameter.
	 */
	private static final String PARAMETER = "parameter";

	/**
	 * Filter base on version metadata.
	 */
	private static final String VERSION = "version";

	/**
	 * Sign of no match any rule.
	 */
	private static final int NO_MATCH = -1;

	/**
	 * Avoid loss of accuracy.
	 */
	private static final double KEEP_ACCURACY = 1.0;

	/**
	 * Composite route. todo
	 */
	private AbstractServerPredicate predicate;

	/**
	 * Match condition util.
	 */
	private final ConditionMatchUtil conditionMatchUtil = new ConditionMatchUtil();

	/**
	 * Load Balance Util.
	 */
	private final LoadBalanceUtil loadBalanceUtil = new LoadBalanceUtil();

	@Autowired
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	@Autowired
	private NacosServiceManager nacosServiceManager;

	@Autowired
	private RouteDataRepository routeDataRepository;

	@Autowired
	private RouterProperties routerProperties;

	@Autowired
	private TargetServiceChangedPublisher targetServiceChangedPublisher;

	@Override
	public Server choose(Object key) {
		try {
			final DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
			String targetServiceName = loadBalancer.getName();
			targetServiceChangedPublisher.addTargetService(targetServiceName);

			// If routeData isn't present, use normal load balance rule.
			final HashMap<String, List<MatchService>> routeData = routeDataRepository
					.getRouteRule(targetServiceName);
			if (routeData == null) {
				return loadBalanceUtil.loadBalanceByOrdinaryRule(loadBalancer, key,
						routerProperties.getRule());
			}

			// Get instances from register-center.
			String group = this.nacosDiscoveryProperties.getGroup();
			final NamingService namingService = nacosServiceManager.getNamingService();
			final List<Instance> instances = namingService
					.selectInstances(targetServiceName, group, true);
			if (CollectionUtils.isEmpty(instances)) {
				LOG.warn("no instance in service {} ", targetServiceName);
				return null;
			}

			// Filter by route rules,the result will be kept in versionSet and weightMap.
			HashSet<String> versionSet = new HashSet<>();
			HashMap<String, Integer> weightMap = new HashMap<>();
			HashSet<String> fallbackVersionSet = new HashSet<>();
			HashMap<String, Integer> fallbackWeightMap = new HashMap<>();

			serviceFilter(targetServiceName, versionSet, weightMap, fallbackVersionSet,
					fallbackWeightMap);

			// Filter instances by versionSet and weightMap.
			double[] weightArray = new double[instances.size()];
			HashMap<String, List<Instance>> instanceMap = new HashMap<>();
			for (Instance instance : instances) {
				String version = instance.getMetadata().get(VERSION);
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
					String version = instance.getMetadata().get(VERSION);
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

	@Override
	public AbstractServerPredicate getPredicate() {
		return this.predicate;
	}

	private void serviceFilter(String targetServiceName, HashSet<String> versionSet,
			HashMap<String, Integer> weightMap, HashSet<String> fallbackVersionSet,
			HashMap<String, Integer> fallbackWeightMap) {
		// Get request metadata.
		final HttpServletRequest request = RequestContext.getRequest();
		final Enumeration<String> headerNames = request.getHeaderNames();
		HashMap<String, String> requestHeaders = new HashMap<>();
		if (headerNames != null) {
			while (headerNames.hasMoreElements()) {
				String name = headerNames.nextElement();
				String value = request.getHeader(name);
				requestHeaders.put(name, value);
			}
		}
		final Map<String, String[]> parameterMap = request.getParameterMap();
		int defaultVersionWeight = RouteDataRepository.SUM_WEIGHT;
		boolean isMatch = false;

		// Parse rule.
		if (requestHeaders.size() > 0) {
			for (String keyName : requestHeaders.keySet()) {
				int weight = matchRule(targetServiceName, keyName, requestHeaders,
						parameterMap, request, versionSet, weightMap, fallbackVersionSet,
						fallbackWeightMap);
				if (weight != NO_MATCH) {
					isMatch = true;
					defaultVersionWeight -= weight;
					break;
				}
			}
		}

		if (!isMatch && parameterMap != null) {
			for (String keyName : parameterMap.keySet()) {
				int weight = matchRule(targetServiceName, keyName, requestHeaders,
						parameterMap, request, versionSet, weightMap, fallbackVersionSet,
						fallbackWeightMap);
				if (weight != NO_MATCH) {
					isMatch = true;
					defaultVersionWeight -= weight;
					break;
				}
			}
		}

		final List<MatchService> pathRules = routeDataRepository
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
		if (defaultVersionWeight > RouteDataRepository.MIN_WEIGHT) {
			String defaultRouteVersion = routeDataRepository
					.getDefaultRouteVersion(targetServiceName);
			versionSet.add(defaultRouteVersion);
			weightMap.put(defaultRouteVersion, defaultVersionWeight);
		}

	}

	private int matchRule(String targetServiceName, String keyName,
			final HashMap<String, String> requestHeaders,
			final Map<String, String[]> parameterMap, final HttpServletRequest request,
			HashSet<String> versionSet, HashMap<String, Integer> weightMap,
			HashSet<String> fallbackVersionSet,
			HashMap<String, Integer> fallbackWeightMap) {
		final List<MatchService> matchServiceList = routeDataRepository
				.getRouteRule(targetServiceName).get(keyName);
		if (matchServiceList == null) {
			return NO_MATCH;
		}
		for (MatchService matchService : matchServiceList) {
			final List<RouteRule> ruleList = matchService.getRuleList();
			String version = matchService.getVersion();
			Integer weight = matchService.getWeight();
			String fallback = matchService.getFallback();
			boolean isMatchRule = true;
			for (RouteRule routeRule : ruleList) {
				String type = routeRule.getType();
				switch (type) {
				case PATH:
					isMatchRule = parseRequestPath(routeRule, request);
					break;
				case HEADER:
					isMatchRule = parseRequestHeader(routeRule, requestHeaders);
					break;
				case PARAMETER:
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
		return NO_MATCH;
	}

	private boolean parseRequestPath(final RouteRule routeRule,
			final HttpServletRequest request) {
		String condition = routeRule.getCondition();
		String value = routeRule.getValue();
		String uri = request.getRequestURI();
		return conditionMatch(condition, value, uri);
	}

	private boolean parseRequestHeader(final RouteRule routeRule,
			final HashMap<String, String> requestHeaders) {
		if (requestHeaders.size() == 0) {
			return false;
		}
		String condition = routeRule.getCondition();
		String value = routeRule.getValue();
		String headerValue = requestHeaders.get(routeRule.getKey());
		return conditionMatch(condition, value, headerValue);
	}

	private boolean parseRequestParameter(final RouteRule routeRule,
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
			return conditionMatchUtil.exactMatch(str, comparator);
		case ConditionMatchUtil.REGEX:
			return conditionMatchUtil.regexMatch(str, comparator);
		case ConditionMatchUtil.PREFIX:
			return conditionMatchUtil.prefixMatch(str, comparator);
		case ConditionMatchUtil.CONTAIN:
			return conditionMatchUtil.containMatch(str, comparator);
		case ConditionMatchUtil.GREATER:
			return conditionMatchUtil.greaterMatch(str, comparator);
		case ConditionMatchUtil.LESS:
			return conditionMatchUtil.lessMatch(str, comparator);
		case ConditionMatchUtil.NOT_EQUAL:
			return conditionMatchUtil.noEqualMatch(str, comparator);
		default:
			throw new UnsupportedOperationException(
					"unsupported string compare operation");
		}
	}

	private Server chooseServerByWeight(final HashMap<String, List<Instance>> instanceMap,
			final HashMap<String, Integer> weightMap, final double[] weightArray) {
		int index = 0;
		double sum = 0.0D;
		List<Instance> instances = new ArrayList<>();

		for (String version : instanceMap.keySet()) {
			int weight = weightMap.get(version);
			List<Instance> instanceList = instanceMap.get(version);
			for (Instance instance : instanceList) {
				instances.add(instance);
				weightArray[index] = KEEP_ACCURACY * weight / instanceList.size() + sum;
				sum = weightArray[index];
				index++;
			}
		}

		if (sum > RouteDataRepository.SUM_WEIGHT) {
			LOG.error("Sum of weight has over {} ", RouteDataRepository.SUM_WEIGHT);
		}

		double random = ThreadLocalRandom.current().nextDouble(
				RouteDataRepository.MIN_WEIGHT, RouteDataRepository.SUM_WEIGHT);
		int chooseServiceIndex = Arrays.binarySearch(weightArray, random);
		if (chooseServiceIndex < 0) {
			chooseServiceIndex = -chooseServiceIndex - 1;
		}

		return new NacosServer(instances.get(chooseServiceIndex));
	}

}
