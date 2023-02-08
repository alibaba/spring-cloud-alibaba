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

package com.alibaba.cloud.routing.listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.cloud.commons.governance.event.RoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import com.alibaba.cloud.routing.repository.FilterService;
import com.alibaba.cloud.routing.repository.RoutingDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationListener;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class RoutingDataListener implements ApplicationListener<RoutingDataChangedEvent> {

	private static final Logger log = LoggerFactory.getLogger(RoutingDataListener.class);

	private RoutingDataRepository routingDataRepository;

	private FilterService filterService;

	public RoutingDataListener(RoutingDataRepository routeDataRepository,
			FilterService filterService) {
		this.routingDataRepository = routeDataRepository;
		this.filterService = filterService;
	}

	@Override
	public void onApplicationEvent(RoutingDataChangedEvent event) {
		try {
			Collection<UnifiedRoutingDataStructure> unifiedRoutingDataStructureList = event
					.getUntiedRouterDataStructureList();

			// Filter service.
			// todo can cache the result
			HashSet<String> definitionFeignService = filterService
					.getDefinitionFeignService(unifiedRoutingDataStructureList.size());
			List<UnifiedRoutingDataStructure> routeDatalist = unifiedRoutingDataStructureList
					.stream()
					.filter(unifiedRouteDataStructure -> definitionFeignService
							.contains(unifiedRouteDataStructure.getTargetService()))
					.collect(Collectors.toList());

			routingDataRepository.updateRouteData(routeDatalist);
		}
		catch (Exception e) {
			log.error("Failed to update route data", e);
		}
	}

}
