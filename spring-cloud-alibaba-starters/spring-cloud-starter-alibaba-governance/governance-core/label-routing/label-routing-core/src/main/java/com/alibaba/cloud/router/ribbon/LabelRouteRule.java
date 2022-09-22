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
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.data.cache.RouteDataCache;
import com.alibaba.cloud.data.crd.LabelRouteData;
import com.alibaba.cloud.data.crd.MatchService;
import com.alibaba.cloud.data.crd.rule.RouteRule;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.cloud.router.cache.RequestCache;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.PredicateBasedRule;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;

/**
 * @author HH
 */
public class LabelRouteRule extends PredicateBasedRule {

	private static final Logger LOGGER = LoggerFactory.getLogger(LabelRouteRule.class);

	private AbstractServerPredicate predicate = null;

	@Autowired
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	@Autowired
	private NacosServiceManager nacosServiceManager;

	@Autowired
	private DiscoveryClient client;

	private RouteDataCache routeDataCache = new RouteDataCache();

	@Autowired
	private RequestCache requestCache;

	private HashMap<String, String> requestHeaders = new HashMap<>();

	private static final String HEADER = "header";

	private static final String PARAMETER = "parameter";

	private static final String VERSION = "version";

	private final HashSet<String> versionSet = new HashSet<>();

	private final HashMap<String, Integer> weightMap = new HashMap<>();

	private final List<Instance> instanceList = new ArrayList<>();

	private String targetService;

	private int[] weightArray;

	@Override
	public Server choose(Object key) {
		try {
			String group = this.nacosDiscoveryProperties.getGroup();
			DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
			String name = loadBalancer.getName();
			this.targetService = name;

			NamingService namingService = nacosServiceManager.getNamingService();
			List<Instance> instances = namingService.selectInstances(name, group, true);

			serviceFilter(name);

			if (CollectionUtils.isEmpty(instances)) {
				LOGGER.warn("no instance in service {}", name);
				return null;
			}

			for (Instance instance : instances) {
				Map<String, String> metadata = instance.getMetadata();
				String version = metadata.get("version");
				if (versionSet.contains(version)) {
					instanceList.add(instance);
				}
			}

			weightArray = new int[instances.size()];

			return chooseServerByWeight(instances);

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


	private void serviceFilter(String targetServiceName) {
		HttpServletRequest request = requestCache.getRequest();
		Map<String, String[]> parameterMap = request.getParameterMap();
		Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames != null) {
			while (headerNames.hasMoreElements()) {
				String name = headerNames.nextElement();
				String value = request.getHeader(name);
				requestHeaders.put(name, value);
			}
		}

		LabelRouteData routeData = routeDataCache.getRouteData(targetServiceName);
		List<MatchService> matchRouteList = routeData.getMatchRouteList();

		int defaultVersionWeight = 100;
		for (MatchService matchService : matchRouteList) {
			List<RouteRule> ruleList = matchService.getRuleList();
			for (RouteRule routeRule : ruleList) {
				if (HEADER.equalsIgnoreCase(routeRule.getType())) {
					if (!routeRule.getValue().equals(requestHeaders.get(routeRule.getKey()))) {
						break;
					}
				}
				if (PARAMETER.equalsIgnoreCase(routeRule.getType())) {
					if (!routeRule.getValue().equals(parameterMap.get(routeRule.getKey())[0])) {
						break;
					}
				}
			}
			versionSet.add(matchService.getVersion());
			weightMap.put(matchService.getVersion(), matchService.getWeight());
			defaultVersionWeight -= matchService.getWeight();
		}

		versionSet.add(routeData.getDefaultRouteVersion());
		if (defaultVersionWeight > 0) {
			weightMap.put(routeData.getDefaultRouteVersion(), defaultVersionWeight);
		}
	}

	private Server chooseServerByWeight(List<Instance> instances) {
		int index = 0;
		int sum = 0;

		for (Instance instance : instances) {
			String version = instance.getMetadata().get(VERSION);
			Integer weight = weightMap.get(version);

			if (weight == null || weight < 0 || weight > 100) {
				LOGGER.error("The weight of provider = {} version = {} had set error,please check it",
						targetService, version);
			}

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

		LOGGER.info("choose instance = {}", instances.get(chooseServiceIndex));

		return server;
	}
}
