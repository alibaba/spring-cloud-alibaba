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

package com.alibaba.cloud.router.data.controlplane;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.cloud.router.data.crd.UntiedRouteDataStructure;
import com.alibaba.cloud.router.data.repository.FilterService;
import com.alibaba.cloud.router.data.repository.RouteDataRepository;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author HH
 */
public class ControlPlaneConnection implements ControlPlane {

	/**
	 * Sign of init.
	 */
	private boolean isRepositoryInitialized = false;

	@Autowired
	private RouteDataRepository routeDataRepository;

	@Autowired
	private FilterService filterService;

	@Override
	public void getDataFromControlPlane(
			List<UntiedRouteDataStructure> untiedRouterDataStructureList) {
		//Filter service.
		HashSet<String> definitionFeignService = filterService
				.getDefinitionFeignService(untiedRouterDataStructureList.size());
		List<UntiedRouteDataStructure> routeDatalist = untiedRouterDataStructureList
				.stream()
				.filter(untiedRouteDataStructure -> definitionFeignService
						.contains(untiedRouteDataStructure.getTargetService())).collect(Collectors.toList());

		//Put data into repository.
		if (!isRepositoryInitialized) {
			routeDataRepository.init(routeDatalist);
			isRepositoryInitialized = true;
		}
		else {
			routeDataRepository.updateRouteData(routeDatalist);
		}
	}

}
