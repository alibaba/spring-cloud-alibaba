/*
 * Copyright 2013-2022 the original author or authors.
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

import org.springframework.cloud.client.ServiceInstance;

/**
 * Service cache.
 * <p>
 * Cache serviceIds and corresponding instances in Nacos.
 *
 * @author freeman
 * @since 2022.0
 */
public final class ServiceCache {

	private ServiceCache() {
	}

	private static List<String> services = Collections.emptyList();

	private static Map<String, List<ServiceInstance>> instancesMap = new ConcurrentHashMap<>();

	public static void setInstances(String serviceId, List<ServiceInstance> instances) {
		instancesMap.put(serviceId, Collections.unmodifiableList(instances));
	}

	public static List<ServiceInstance> getInstances(String serviceId) {
		return Optional.ofNullable(instancesMap.get(serviceId)).orElse(Collections.emptyList());
	}

	public static void set(List<String> newServices) {
		services = Collections.unmodifiableList(newServices);
	}

	public static List<String> get() {
		return services;
	}

}
