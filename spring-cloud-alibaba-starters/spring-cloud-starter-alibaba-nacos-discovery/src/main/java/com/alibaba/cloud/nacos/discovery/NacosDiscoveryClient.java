/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.nacos.discovery;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * @author xiaojing
 * @author renhaojun
 * @author echooymxq
 * @author freeman
 */
public class NacosDiscoveryClient implements DiscoveryClient {

	private static final Logger log = LoggerFactory.getLogger(NacosDiscoveryClient.class);

	/**
	 * Nacos Discovery Client Description.
	 */
	public static final String DESCRIPTION = "Spring Cloud Nacos Discovery Client";

	private NacosServiceDiscovery serviceDiscovery;

	@Value("${spring.cloud.nacos.discovery.failure-tolerance-enabled:false}")
	private boolean failureToleranceEnabled;

	public NacosDiscoveryClient(NacosServiceDiscovery nacosServiceDiscovery) {
		this.serviceDiscovery = nacosServiceDiscovery;
	}

	@Override
	public String description() {
		return DESCRIPTION;
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		try {
			return Optional.of(serviceDiscovery.getInstances(serviceId))
					.map(instances -> {
						ServiceCache.setInstances(serviceId, instances);
						return instances;
					}).get();
		}
		catch (Exception e) {
			if (failureToleranceEnabled) {
				return ServiceCache.getInstances(serviceId);
			}
			throw new RuntimeException(
					"Can not get hosts from nacos server. serviceId: " + serviceId, e);
		}
	}

	@Override
	public List<String> getServices() {
		try {
			return Optional.of(serviceDiscovery.getServices()).map(services -> {
				ServiceCache.setServiceIds(services);
				return services;
			}).get();
		}
		catch (Exception e) {
			log.error("get service name from nacos server failed.", e);
			return failureToleranceEnabled ? ServiceCache.getServiceIds()
					: Collections.emptyList();
		}
	}

}
