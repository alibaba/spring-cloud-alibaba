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

package com.alibaba.cloud.commons.governance.event;

import java.util.Collection;

import com.alibaba.cloud.commons.governance.labelrouting.UnifiedRouteDataStructure;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 */
public class LabelRoutingDataChangedEvent extends GovernanceEvent {

	/**
	 * Configuration for Label Routing.
	 */
	private final Collection<UnifiedRouteDataStructure> untiedRouterDataStructureList;

	public LabelRoutingDataChangedEvent(Object source,
			Collection<UnifiedRouteDataStructure> untiedRouterDataStructureList) {
		super(source);
		this.untiedRouterDataStructureList = untiedRouterDataStructureList;
	}

	public Collection<UnifiedRouteDataStructure> getUntiedRouterDataStructureList() {
		return untiedRouterDataStructureList;
	}

}
