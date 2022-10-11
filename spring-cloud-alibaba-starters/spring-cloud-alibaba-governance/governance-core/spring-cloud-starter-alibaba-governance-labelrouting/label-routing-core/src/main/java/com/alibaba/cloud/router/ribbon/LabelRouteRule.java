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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.cloud.router.context.RequestContext;
import com.alibaba.cloud.router.data.crd.LabelRouteData;
import com.alibaba.cloud.router.data.crd.MatchService;
import com.alibaba.cloud.router.data.crd.rule.RouteRule;
import com.alibaba.cloud.router.data.repository.RouteDataRepository;
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
public class LabelRouteRule extends PredicateBasedRule {

	private static final Logger LOGGER = LoggerFactory.getLogger(LabelRouteRule.class);

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
	 * If do not set weight value,it will be set 100 by default.
	 */
	private static final int DEFAULT_WEIGHT = 100;

	/**
	 * Sum of all version's weight.
	 */
	private static final int SUM_WEIGHT = 100;

	/**
	 * Weight value can't less than it.
	 */
	private static final int MIN_WEIGHT = 0;

	/**
	 * Composite route.
	 * todo
	 */
	private AbstractServerPredicate predicate;

	@Autowired
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	@Autowired
	private NacosServiceManager nacosServiceManager;

	@Autowired
	private RouteDataRepository routeDataRepository;

	@Autowired
	private RequestContext requestContext;

	@Override
	public Server choose(Object key) {
		try {
			String group = this.nacosDiscoveryProperties.getGroup();
			DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
			String name = loadBalancer.getName();

			NamingService namingService = nacosServiceManager.getNamingService();
			List<Instance> instances = namingService.selectInstances(name, group, true);

			HashSet<String> versionSet = new HashSet<>();
			HashMap<String, Integer> weightMap = new HashMap<>();

			//filter by route rules,the result will be kept in versionSet and weightMap.
			serviceFilter(name, versionSet, weightMap);

			if (CollectionUtils.isEmpty(instances)) {
				LOGGER.warn("no instance in service {}", name);
				return null;
			}

			int[] weightArray = new int[instances.size()];
			List<Instance> instanceList = new ArrayList<>();

			for (Instance instance : instances) {
				Map<String, String> metadata = instance.getMetadata();
				String version = metadata.get("version");
				if (versionSet.contains(version)) {
					instanceList.add(instance);
				}
			}

			return chooseServerByWeight(instanceList, name, weightMap, weightArray);

		}
		catch (Exception e) {
			LOGGER.warn("LabelRouteRule error", e);
			return null;
		}
	}

	@Override
	public AbstractServerPredicate getPredicate() {
		return this.predicate;
	}

	private void serviceFilter(String targetServiceName, HashSet<String> versionSet,
			HashMap<String, Integer> weightMap) {
		final Optional<LabelRouteData> routeData = Optional
				.ofNullable(routeDataRepository.getRouteData(targetServiceName));
		if (!routeData.isPresent()) {
			return;
		}
		final Optional<List<MatchService>> matchRouteList = Optional
				.ofNullable(routeData.get().getMatchRouteList());
		if (!matchRouteList.isPresent()) {
			LOGGER.warn("Target service = {} rule is empty", targetServiceName);
		}

		final HttpServletRequest request = requestContext.getRequest(true);
		final Optional<Enumeration<String>> headerNames = Optional
				.ofNullable(request.getHeaderNames());
		final Optional<Map<String, String[]>> parameterMap = Optional
				.ofNullable(request.getParameterMap());
		HashMap<String, String> requestHeaders = new HashMap<>();

		if (headerNames.isPresent()) {
			while (headerNames.get().hasMoreElements()) {
				String name = headerNames.get().nextElement();
				String value = request.getHeader(name);
				requestHeaders.put(name, value);
			}
		}

		int defaultVersionWeight = SUM_WEIGHT;
		for (MatchService matchService : matchRouteList.get()) {
			Optional<List<RouteRule>> ruleList = Optional
					.ofNullable(matchService.getRuleList());
			Optional<String> version = Optional.ofNullable(matchService.getVersion());
			Integer weight = matchService.getWeight();

			if (!ruleList.isPresent() || ruleList.get().size() == 0) {
				continue;
			}
			if (weight == null) {
				weight = DEFAULT_WEIGHT;
			}
			if (!version.isPresent()) {
				LOGGER.error("Target service = {} rule = {} lose version,please check it",
						targetServiceName, matchService);
			}
			if (weight < MIN_WEIGHT || weight > SUM_WEIGHT) {
				LOGGER.error(
						"The weight of provider = {} version = {} had set error,please check it",
						targetServiceName, version);
			}

			boolean isMatchRule = true;
			for (RouteRule routeRule : ruleList.get()) {
				if (PATH.equalsIgnoreCase(routeRule.getType())) {
					if (!routeRule.getValue().equals(request.getRequestURI())) {
						isMatchRule = false;
						break;
					}
				}
				if (HEADER.equalsIgnoreCase(routeRule.getType())) {
					if (!headerNames.isPresent()
							|| requestHeaders.size() == 0
							|| !routeRule.getValue().equals(requestHeaders.get(routeRule.getKey()))) {
						isMatchRule = false;
						break;
					}
				}
				if (PARAMETER.equalsIgnoreCase(routeRule.getType())) {
					if (!parameterMap.isPresent()
							|| parameterMap.get().size() == 0
							|| !routeRule.getValue().equals(parameterMap.get().get(routeRule.getKey())[0])) {
						isMatchRule = false;
						break;
					}
				}
			}
			if (!isMatchRule) {
				continue;
			}
			versionSet.add(version.get());
			weightMap.put(version.get(), weight);
			defaultVersionWeight -= weight;
		}

		if (defaultVersionWeight > MIN_WEIGHT) {
			versionSet.add(routeData.get().getDefaultRouteVersion());
			weightMap.put(routeData.get().getDefaultRouteVersion(), defaultVersionWeight);
		}

	}

	private Server chooseServerByWeight(List<Instance> instances, String targetService,
			HashMap<String, Integer> weightMap, int[] weightArray) {
		int index = 0;
		int sum = 0;

		for (Instance instance : instances) {
			String version = instance.getMetadata().get(VERSION);
			Integer weight = weightMap.get(version);
			weightArray[index] = weight + sum;
			sum = weightArray[index];
			index++;
		}

		int random = ThreadLocalRandom.current().nextInt(1, 101);
		int chooseServiceIndex = 0;
		for (int i = 0; i < weightArray.length; i++) {
			if (random < weightArray[i]) {
				chooseServiceIndex = i;
				break;
			}
		}
		Server server = new NacosServer(instances.get(chooseServiceIndex));

		return server;
	}

}
