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

import java.util.List;

import com.alibaba.cloud.router.data.crd.UntiedRouteDataStructure;
import com.alibaba.cloud.router.data.repository.RouteDataRepository;


/**
 * @author HH
 */
public class ControlPlaneConnection implements ControlPlane {

	private RouteDataRepository routeDataRepository;

	private boolean cacheInitialized = false;

	public ControlPlaneConnection(RouteDataRepository routeDataRepository) {
		this.routeDataRepository = routeDataRepository;
	}

	@Override
	public void getDataFromControlSurface(
			List<UntiedRouteDataStructure> untiedRouterDataStructureList) {
		if (!cacheInitialized) {
			routeDataRepository.init(untiedRouterDataStructureList);
			cacheInitialized = true;
		}
		else {
			routeDataRepository.updateRouteData(untiedRouterDataStructureList);
		}
	}

}
