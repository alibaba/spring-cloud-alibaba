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

package com.alibaba.cloud.router.listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.cloud.commons.governance.event.LabelRoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.labelrouting.UnifiedRouteDataStructure;
import com.alibaba.cloud.router.repository.FilterService;
import com.alibaba.cloud.router.repository.RouteDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationListener;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 */
public class LabelRouteDataListener
		implements ApplicationListener<LabelRoutingDataChangedEvent> {

	private static final Logger log = LoggerFactory
			.getLogger(LabelRouteDataListener.class);

	private RouteDataRepository routeDataRepository;

	private FilterService filterService;

	public LabelRouteDataListener(RouteDataRepository routeDataRepository,
			FilterService filterService) {
		this.routeDataRepository = routeDataRepository;
		this.filterService = filterService;
	}

	@Override
	public void onApplicationEvent(LabelRoutingDataChangedEvent event) {
		try {
			Collection<UnifiedRouteDataStructure> untiedRouterDataStructureList = event
					.getUntiedRouterDataStructureList();

			// Filter service.
			// todo can cache the result
			HashSet<String> definitionFeignService = filterService
					.getDefinitionFeignService(untiedRouterDataStructureList.size());
			List<UnifiedRouteDataStructure> routeDatalist = untiedRouterDataStructureList
					.stream()
					.filter(unifiedRouteDataStructure -> definitionFeignService
							.contains(unifiedRouteDataStructure.getTargetService()))
					.collect(Collectors.toList());

			routeDataRepository.updateRouteData(routeDatalist);
		}
		catch (Exception e) {
			log.error("Failed to update route data", e);
		}
	}

}
