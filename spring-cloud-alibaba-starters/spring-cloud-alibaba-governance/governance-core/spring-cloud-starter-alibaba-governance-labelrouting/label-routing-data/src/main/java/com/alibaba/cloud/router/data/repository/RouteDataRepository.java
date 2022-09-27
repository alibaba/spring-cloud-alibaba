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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.cloud.router.data.crd.LabelRouteData;
import com.alibaba.cloud.router.data.crd.UntiedRouteDataStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author HH
 */
public class RouteDataRepository {

	private static final Logger log = LoggerFactory.getLogger(RouteDataRepository.class);

	private ConcurrentHashMap<String, LabelRouteData> routeCache = new ConcurrentHashMap<>();

	private boolean routeDataChanged = false;

	private AtomicInteger waitUpdateIndex = new AtomicInteger(-1);

	private AtomicInteger updateIndex = new AtomicInteger(-1);

	private List<UntiedRouteDataStructure> routeDataList;

	public void init(List<UntiedRouteDataStructure> routerDataList) {
		// Try to avoid capacity expansion, and space is used to exchange time.
		int initCacheSize = routerDataList.size() * 2;
		routeCache = new ConcurrentHashMap<>(initCacheSize);

		putRouteData(routerDataList);
	}

	public void updateRouteData(List<UntiedRouteDataStructure> routerDataList) {
		this.routeDataList = routerDataList;
		routeDataChanged = true;

		updateRouteData();
	}

	private void updateRouteData() {
		while (routeDataChanged) {
			int routeDataListSize = routeDataList.size();

			if (waitUpdateIndex.get() == routeDataListSize) {
				return;
			}

			int i = waitUpdateIndex.incrementAndGet();

			// avoid generate critical condition.
			if (i > routeDataListSize) {
				UntiedRouteDataStructure routerData = routeDataList.get(i);
				LabelRouteData labelRouteData = routeCache
						.get(routerData.getTargetService());

				if (!routerData.getLabelRouteData().equals(labelRouteData)) {
					putRouteData(routerData);
				}
				int updateNumber = updateIndex.incrementAndGet();

				if (updateNumber > routeDataListSize) {
					routeDataChanged = false;
				}
			}
		}
	}

	private void putRouteData(UntiedRouteDataStructure routerData) {
		LabelRouteData putLabelRouteData = routeCache.put(routerData.getTargetService(),
				routerData.getLabelRouteData());
		if (putLabelRouteData == null) {
			log.warn("Label route rule:" + routerData + "failed to add to router cache");
		}
	}

	private void putRouteData(List<UntiedRouteDataStructure> routerDataList) {
		LabelRouteData putLabelRouteData = null;

		for (UntiedRouteDataStructure routerData : routerDataList) {
			putLabelRouteData = routeCache.put(routerData.getTargetService(),
					routerData.getLabelRouteData());
			if (putLabelRouteData != null) {
				log.info("Label route rule:" + routerData
						+ "had been add to router cache");
			}
			else {
				log.warn("Label route rule:" + routerData
						+ "failed to add to router cache");
			}
		}
	}

	public LabelRouteData getRouteData(String targetService) {
		// double check.
		while (routeDataChanged) {
			updateRouteData();

			if (routeDataChanged) {
				int matchIndex = 0;
				for (UntiedRouteDataStructure routeData : routeDataList) {
					if (targetService.equals(routeData.getTargetService())) {
						break;
					}
					matchIndex++;
				}
				if (matchIndex <= updateIndex.get()) {
					return routeCache.get(targetService);
				}
			}
		}

		return routeCache.get(targetService);
	}
}
