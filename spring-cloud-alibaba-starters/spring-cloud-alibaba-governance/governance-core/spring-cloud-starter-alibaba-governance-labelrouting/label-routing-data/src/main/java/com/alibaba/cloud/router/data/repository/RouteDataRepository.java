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

package com.alibaba.cloud.router.data.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.cloud.router.data.crd.LabelRouteData;
import com.alibaba.cloud.router.data.crd.MatchService;
import com.alibaba.cloud.router.data.crd.UntiedRouteDataStructure;
import com.alibaba.cloud.router.data.crd.rule.RouteRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author HH
 */
public class RouteDataRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(RouteDataRepository.class);

	/**
	 * Key is service name,value is hashmap,which key is single RouteRule key,value is match service.
	 * Use double hash index to parse route rule.
	 */
	private ConcurrentHashMap<String, HashMap<String, List<MatchService>>> routeCache;

	/**
	 * Control plane original data structure,use to accelerate compare if change.
	 */
	private HashMap<String, LabelRouteData> originalRouteData;

	/**
	 * Sign of Route rule change.
	 */
	private boolean routeDataChanged = false;

	/**
	 * Support Parsing Rules from path,only URI at present.
	 */
	private static final String PATH = "path";

	/**
	 * Use to update route data.
	 */
	private List<UntiedRouteDataStructure> routeDataList;

	/**
	 * Contain rule of single path rule.
	 */
	private HashMap<String, List<MatchService>> pathRuleMap;

	public void init(final List<UntiedRouteDataStructure> routerDataList) {
		int initCacheSize = routerDataList.size();
		routeCache = new ConcurrentHashMap<>(initCacheSize);
		originalRouteData = new HashMap<>(initCacheSize);

		putRouteData(routerDataList);
	}

	public void updateRouteData(final List<UntiedRouteDataStructure> routerDataList) {
		this.routeDataList = routerDataList;
		routeDataChanged = true;

		updateRouteData();
	}

	/**
	 *
	 * @return Finished updating data index.
	 */
	private int updateRouteData() {
		final AtomicInteger waitUpdateIndex = new AtomicInteger(-1);
		final AtomicInteger updateIndex = new AtomicInteger(-1);

		while (routeDataChanged) {
			int routeDataListSize = routeDataList.size();
			//If all tasks had been distributed
			if (waitUpdateIndex.get() == routeDataListSize) {
				return updateIndex.get();
			}
			//If not,get a task.
			int i = waitUpdateIndex.incrementAndGet();

			// May be multi-thread competition,avoid critical condition.
			if (i < routeDataListSize) {
				UntiedRouteDataStructure routerData = routeDataList.get(i);
				LabelRouteData labelRouteData = originalRouteData
						.get(routerData.getTargetService());

				if (!routerData.getLabelRouteData().equals(labelRouteData)) {
					buildHashIndex(routerData);
					originalRouteData.put(routerData.getTargetService(), routerData.getLabelRouteData());
				}
				int updateNumber = updateIndex.incrementAndGet();

				if (updateNumber >= routeDataListSize - 1) {
					routeDataChanged = false;
				}
			}
		}

		return updateIndex.get();
	}

	private void putRouteData(final List<UntiedRouteDataStructure> routerDataList) {
		for (UntiedRouteDataStructure routerData : routerDataList) {
			buildHashIndex(routerData);
			originalRouteData.put(routerData.getTargetService(), routerData.getLabelRouteData());
		}
	}

	private void buildHashIndex(final UntiedRouteDataStructure routerData) {
		final List<MatchService> matchRouteList = routerData.getLabelRouteData().getMatchRouteList();
		HashMap<String, List<MatchService>> singleRuleMap = new HashMap<>();

		for (MatchService matchService : matchRouteList) {
			List<RouteRule> ruleList = matchService.getRuleList();

			//Take out the path label separately, because there is no key for hash index.
			if (ruleList.size() == 1 && PATH.equalsIgnoreCase(ruleList.get(0).getType())) {
				List<MatchService> matchServiceList = pathRuleMap.get(routerData.getTargetService());
				if (matchServiceList == null) {
					matchServiceList = new ArrayList<>();
				}
				matchServiceList.add(matchService);
				pathRuleMap.put(routerData.getTargetService(), matchServiceList);
				continue;
			}
			for (RouteRule routeRule : ruleList) {
				List<MatchService> matchServiceList = singleRuleMap.get(routeRule.getKey());
				if (matchServiceList == null) {
					matchServiceList = new ArrayList<>();
				}
				matchServiceList.add(matchService);
				singleRuleMap.put(routeRule.getKey(), matchServiceList);
			}
		}
		routeCache.put(routerData.getTargetService(), singleRuleMap);
	}

	public HashMap<String, List<MatchService>> getRouteData(String targetService) {
		updateData(targetService);
		return routeCache == null ? null : routeCache.get(targetService);
	}

	public LabelRouteData getOriginalRouteData(String targetService) {
		updateData(targetService);
		return originalRouteData == null ? null : originalRouteData.get(targetService);
	}

	public List<MatchService> getPathRules(String targetService) {
		updateData(targetService);
		return pathRuleMap == null ? null : pathRuleMap.get(targetService);
	}

	private void updateData(String targetService) {
		while (routeDataChanged) {
			//Update label rule data.
			int updateIndex = updateRouteData();

			// Double check.
			if (routeDataChanged) {
				int matchIndex = 0;
				//Find targetService index in list.
				for (UntiedRouteDataStructure routeData : routeDataList) {
					if (targetService.equals(routeData.getTargetService())) {
						break;
					}
					matchIndex++;
				}
				//If match index has update.
				if (matchIndex <= updateIndex) {
					return;
				}
			}
		}
	}
}
