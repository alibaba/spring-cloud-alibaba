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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.nacos.discovery.reactive.NacosReactiveDiscoveryClient;

import org.springframework.cloud.client.ServiceInstance;

/**
 * Service cache.
 * <p>
 * Cache serviceIds and corresponding instances in Nacos.
 * <p>
 * It's very useful to query services and instances on runtime, but it's not real-time,
 * depends on {@link NacosDiscoveryClient} or {@link NacosReactiveDiscoveryClient}
 * {@code getServices(), getInstances(..)} invoke.
 *
 * @author freeman
 * @since 2021.0.1.0
 */
public final class ServiceCache {

	private ServiceCache() {
	}

	private static List<String> services = Collections.emptyList();

	private static Map<String, List<ServiceInstance>> instancesMap = new ConcurrentHashMap<>();

	/**
	 * Set instances for specific service.
	 * @param serviceId service id
	 * @param instances service instances
	 */
	public static void setInstances(String serviceId, List<ServiceInstance> instances) {
		instancesMap.put(serviceId, Collections.unmodifiableList(instances));
	}

	/**
	 * Get instances for specific service.
	 * @param serviceId service id
	 * @return service instances
	 */
	public static List<ServiceInstance> getInstances(String serviceId) {
		return Optional.ofNullable(instancesMap.get(serviceId))
				.orElse(Collections.emptyList());
	}

	/**
	 * Set all services.
	 * @param serviceIds all services
	 * @deprecated since 2021.0.1.1, use {@link #setServiceIds(List)} instead.
	 */
	@Deprecated
	public static void set(List<String> serviceIds) {
		services = Collections.unmodifiableList(serviceIds);
	}

	/**
	 * Set all services.
	 * @param serviceIds all services
	 * @since 2021.0.1.1
	 */
	public static void setServiceIds(List<String> serviceIds) {
		services = Collections.unmodifiableList(serviceIds);
	}

	/**
	 * Get all services.
	 * @return all services
	 * @deprecated since 2021.0.1.1, use {@link #getServiceIds()} instead.
	 */
	@Deprecated
	public static List<String> get() {
		return services;
	}

	/**
	 * Get all services.
	 * @return all services
	 * @since 2021.0.1.1
	 */
	public static List<String> getServiceIds() {
		return services;
	}

}
