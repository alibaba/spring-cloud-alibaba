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

import java.util.Objects;

/**
 * @author HH
 */
public class ServiceMetadata {
	/**
	 * service use rule.
	 */
	private String service;

	/**
	 * version of service use rule.
	 */
	private String serviceVersion;

	/**
	 * target service.
	 */
	private String targetService;

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public String getTargetService() {
		return targetService;
	}

	public void setTargetService(String targetService) {
		this.targetService = targetService;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ServiceMetadata that = (ServiceMetadata) o;
		return Objects.equals(service, that.service) && Objects
				.equals(serviceVersion, that.serviceVersion) && Objects.equals(targetService, that.targetService);
	}

	@Override
	public int hashCode() {
		return Objects.hash(service, serviceVersion, targetService);
	}
}
