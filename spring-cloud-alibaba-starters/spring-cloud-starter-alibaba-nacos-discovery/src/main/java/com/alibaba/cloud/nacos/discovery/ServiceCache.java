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
public class ServiceCache {

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
