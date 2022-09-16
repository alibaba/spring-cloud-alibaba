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

package com.alibaba.cloud.data.crd;

/**
 * @author HH
 */
public class UntiedRouteDataStructure {

	private LabelRouteData labelRouteData;

	private ServiceMetadata serviceMetadata;

	public LabelRouteData getLabelRouteData() {
		return labelRouteData;
	}

	public void setLabelRouteData(LabelRouteData labelRouteData) {
		this.labelRouteData = labelRouteData;
	}

	public ServiceMetadata getServiceMetadata() {
		return serviceMetadata;
	}

	public void setServiceMetadata(ServiceMetadata serviceMetadata) {
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public String toString() {
		return "UntiedRouterDataStructure{" +
				"labelRouteData=" + labelRouteData +
				", serviceMetadata=" + serviceMetadata +
				'}';
	}
}
