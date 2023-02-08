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

package com.alibaba.cloud.routing.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.commons.governance.routing.MatchService;
import com.alibaba.cloud.commons.governance.routing.RoutingRule;
import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import com.alibaba.cloud.commons.governance.routing.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.CollectionUtils;

/**
 * @author HH
 * @since 2.2.10-RC1
 */
public class RoutingDataRepository {

	private static final Logger LOG = LoggerFactory
			.getLogger(RoutingDataRepository.class);

	/**
	 * Key is service name,value is hashmap,which key is single RoutingRule key,value is
	 * match service. Use double hash index to parse route rule.
	 */
	private ConcurrentHashMap<String, HashMap<String, List<MatchService>>> routeCache = new ConcurrentHashMap<>();

	/**
	 * The default version of each service.
	 */
	private ConcurrentHashMap<String, String> defaultRoutingVersion = new ConcurrentHashMap<>();

	/**
	 * Sign of path.
	 */
	private static final String PATH = "path";

	/**
	 * Contain rule of single path rule.
	 */
	private ConcurrentHashMap<String, List<MatchService>> pathRuleMap = new ConcurrentHashMap<>();

	/**
	 * If do not set weight value,it will be set 100 by default.
	 */
	private static final int DEFAULT_WEIGHT = 100;

	/**
	 * Sum of all version's weight.
	 */
	public static final int SUM_WEIGHT = 100;

	/**
	 * Weight value can't less than it.
	 */
	public static final int MIN_WEIGHT = 0;

	public void updateRouteData(final List<UnifiedRoutingDataStructure> routeDataList) {
		ConcurrentHashMap<String, HashMap<String, List<MatchService>>> newRouteCache = new ConcurrentHashMap<>();
		ConcurrentHashMap<String, List<MatchService>> newPathRuleMap = new ConcurrentHashMap<>();
		for (UnifiedRoutingDataStructure routeData : routeDataList) {
			nonNullCheck(routeData);
			buildHashIndex(routeData, newRouteCache, newPathRuleMap);
			defaultRoutingVersion.put(routeData.getTargetService(),
					routeData.getLabelRouteRule().getDefaultRouteVersion());
		}
		// Replace it atomically
		this.routeCache = newRouteCache;
		this.pathRuleMap = newPathRuleMap;
	}

	private void nonNullCheck(UnifiedRoutingDataStructure unifiedRoutingDataStructure) {
		String targetService = unifiedRoutingDataStructure.getTargetService();
		if (targetService == null) {
			LOG.error("Lose target Service name.");
		}
		final RoutingRule labelRouteData = unifiedRoutingDataStructure
				.getLabelRouteRule();
		final List<MatchService> matchServiceList = labelRouteData.getMatchRouteList();
		for (MatchService matchService : matchServiceList) {
			final List<Rule> ruleList = matchService.getRuleList();
			String version = matchService.getVersion();
			Integer weight = matchService.getWeight();
			if (CollectionUtils.isEmpty(ruleList)) {
				LOG.error("Rule is empty in version = {} ", version);
			}
			if (version == null) {
				LOG.error("Target service = {} lose version,please check it. ",
						targetService);
			}
			if (weight == null) {
				weight = DEFAULT_WEIGHT;
			}
			if (weight < MIN_WEIGHT || weight > SUM_WEIGHT) {
				LOG.error(
						"The weight of provider = {} version = {} had set error,please check it. ",
						targetService, version);
			}
		}
	}

	private void buildHashIndex(final UnifiedRoutingDataStructure routerData,
			ConcurrentHashMap<String, HashMap<String, List<MatchService>>> newRouteCache,
			ConcurrentHashMap<String, List<MatchService>> newPathRuleMap) {
		final List<MatchService> matchRouteList = routerData.getLabelRouteRule()
				.getMatchRouteList();
		HashMap<String, List<MatchService>> singleRuleMap = new HashMap<>();

		for (MatchService matchService : matchRouteList) {
			List<Rule> ruleList = matchService.getRuleList();

			// Take out the path label separately, because there is no key for hash index.
			if (ruleList.size() == 1
					&& PATH.equalsIgnoreCase(ruleList.get(0).getType())) {
				List<MatchService> matchServiceList = newPathRuleMap
						.get(routerData.getTargetService());
				if (matchServiceList == null) {
					matchServiceList = new ArrayList<>();
				}
				matchServiceList.add(matchService);
				newPathRuleMap.put(routerData.getTargetService(), matchServiceList);
				continue;
			}
			for (Rule routeRule : ruleList) {
				List<MatchService> matchServiceList = singleRuleMap
						.get(routeRule.getKey());
				if (matchServiceList == null) {
					matchServiceList = new ArrayList<>();
				}
				matchServiceList.add(matchService);
				if (routeRule.getKey() != null) {
					singleRuleMap.put(routeRule.getKey(), matchServiceList);
				}
			}
		}
		newRouteCache.put(routerData.getTargetService(), singleRuleMap);
	}

	public HashMap<String, List<MatchService>> getRouteRule(String targetService) {
		return routeCache.get(targetService);
	}

	public String getDefaultRouteVersion(String targetService) {
		return defaultRoutingVersion.get(targetService);
	}

	public List<MatchService> getPathRules(String targetService) {
		return pathRuleMap.get(targetService);
	}

}
