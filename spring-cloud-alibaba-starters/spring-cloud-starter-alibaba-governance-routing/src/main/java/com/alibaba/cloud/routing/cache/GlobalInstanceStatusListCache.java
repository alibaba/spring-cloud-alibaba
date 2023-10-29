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

package com.alibaba.cloud.routing.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.cloud.routing.model.ServiceInstanceInfo;
import com.alibaba.nacos.api.naming.pojo.Instance;

/**
 * @author xqw
 * @author 550588941@qq.com
 */

public final class GlobalInstanceStatusListCache {

	private GlobalInstanceStatusListCache() {
	}

	/**
	 * Global instance cache.
	 * String: service name.
	 * String: ip + port.
	 * ServiceInstanceInfo: 服务实例信息.
	 */
	private static final Map<String, Map<String, ServiceInstanceInfo>> globalServiceCache = new ConcurrentHashMap<>();

	/**
	 * Set instance cache.
	 *
	 * @param targetName service name.
	 * @param instance service instance object {ip + port}.
	 * @param sif service instance info.
	 */
	public static void set(String targetName, Instance instance,
			ServiceInstanceInfo sif) {
		if (globalServiceCache.isEmpty()) {
			Map<String, ServiceInstanceInfo> instanceInfoMap = new ConcurrentHashMap<>();
			instanceInfoMap.put(instance.getIp() + ":" + instance.getPort(), sif);
			globalServiceCache.put(targetName, instanceInfoMap);
		}
		else {
			globalServiceCache.forEach((serviceName, instanceMap) -> {
				Map<String, ServiceInstanceInfo> instanceInfoMap;
				if (serviceName == targetName) {
					// serviceName is exist.
					instanceInfoMap = globalServiceCache.get(targetName);
					instanceInfoMap.put(instance.getIp() + ":" + instance.getIp(), sif);
				}
				else {
					instanceInfoMap = new ConcurrentHashMap<>();
					instanceInfoMap.put(instance.getIp() + ":" + instance.getPort(), sif);
				}
				globalServiceCache.put(targetName, instanceInfoMap);
			});
		}
	}

	/**
	 * Check instance in cache.
	 *
	 * @param target    target.
	 * @param instance  instance object.
	 * @return boolean
	 */
	public static boolean checkContainersInstance(String target, Instance instance) {

		AtomicBoolean flag = new AtomicBoolean(false);

		Map<String, ServiceInstanceInfo> instanceInfoMap = globalServiceCache.get(target);
		if (instanceInfoMap.isEmpty()) {
			return flag.get();
		}
		else {
			instanceInfoMap.forEach((instanceName, sif) -> {
				flag.set(instanceName == instance.getPort() + ":" + instance.getIp());
			});
		}

		return flag.get();
	}

	/**
	 * Return GlobalCache Object.
	 * @return return global instance cache
	 */
	public static Map<String, Map<String, ServiceInstanceInfo>> getAll() {

		return globalServiceCache;
	}

	/**
	 * Get service instance list by service name.
	 * @param targetServiceName service name.
	 * @return instance list.
	 */
	public static Map<String, ServiceInstanceInfo> getInstanceByServiceName(
			String targetServiceName) {

		Map<String, ServiceInstanceInfo> instanceInfoMap = globalServiceCache.get(targetServiceName);
		if (instanceInfoMap.isEmpty()) {
			return null;
		}

		return instanceInfoMap;
	}

	/**
	 * Get instance info by global cache.
	 * @param instanceName instance name
	 * @return sif object
	 */
	public static ServiceInstanceInfo getInstanceByInstanceName(String instanceName) {

		AtomicReference<ServiceInstanceInfo> sif = null;

		globalServiceCache.keySet().forEach((serviceName) -> {
			Map<String, ServiceInstanceInfo> instanceInfoMap = globalServiceCache.get(serviceName);
			sif.set(instanceInfoMap.get(instanceName));
		});

		return sif.get();
	}

	/**
	 * Get no health instance list.
	 *
	 * @return list
	 */
	public static List<ServiceInstanceInfo> getCalledErrorInstance() {

		List<ServiceInstanceInfo> res = new ArrayList<>();

		globalServiceCache.forEach((serviceName, instanceMap) -> {
			Map<String, ServiceInstanceInfo> instanceInfoMap = globalServiceCache.get(serviceName);
			instanceInfoMap.forEach((instanceName, sif) -> {
				ServiceInstanceInfo serviceInstanceInfo = instanceInfoMap.get(instanceName);
				if (serviceInstanceInfo.getConsecutiveErrors() != null) {
					res.add(serviceInstanceInfo);
				}
			});
		});

		return res;
	}

	/**
	 * getServiceUpperLimitRatioNum.
	 *
	 * @param targetServiceName target service name.
	 * @param minHealthPercent minHealthPercent
	 * @return max remove instance num
	 */
	public static int getServiceUpperLimitRatioNum(String targetServiceName,
			double minHealthPercent) {

		int serviceInstanceTotal;
		Map<String, ServiceInstanceInfo> instanceInfoMap = globalServiceCache.get(targetServiceName);
		serviceInstanceTotal = instanceInfoMap.size();

		return (int) Math.floor(serviceInstanceTotal * minHealthPercent);
	}

	/**
	 * Get all instance nums.
	 * @param targetServiceName target service name.
	 * @return remove instance num
	 */
	public static int getInstanceNumByTargetServiceName(String targetServiceName) {

		int serviceInstanceTotal;
		Map<String, ServiceInstanceInfo> instanceInfoMap = globalServiceCache.get(targetServiceName);
		serviceInstanceTotal = instanceInfoMap.size();

		return serviceInstanceTotal;
	}

	/**
	 * Get no health nums.
	 * @param targetServiceName target service name.
	 * @return remove instance num
	 */
	public static int getRemoveInstanceNum(String targetServiceName) {

		AtomicInteger serviceInstanceTotal = new AtomicInteger();

		Map<String, ServiceInstanceInfo> instanceInfoMap = globalServiceCache.get(targetServiceName);
		instanceInfoMap.forEach((instanceName, sif) -> {
			ServiceInstanceInfo serviceInstanceInfo = instanceInfoMap.get(instanceName);
			if (!(serviceInstanceInfo.isStatus())) {
				serviceInstanceTotal.getAndIncrement();
			}
		});

		return serviceInstanceTotal.get();
	}

	public static void setInstanceInfoByInstanceNames(ServiceInstanceInfo sif) {

		String instanceName = sif.getInstance().getIp() + ":"
				+ sif.getInstance().getPort();

		globalServiceCache.forEach((serviceName, instanceMap) -> {
			Map<String, ServiceInstanceInfo> instanceInfoMap = globalServiceCache.get(serviceName);
			instanceInfoMap.put(instanceName, sif);
		});

	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		for (String serviceName : globalServiceCache.keySet()) {
			stringBuilder.append("Service: ").append(serviceName).append("\n");
			Map<String, ServiceInstanceInfo> innerMap = globalServiceCache.get(serviceName);

			for (String instanceId : innerMap.keySet()) {
				ServiceInstanceInfo instanceInfo = innerMap.get(instanceId);
				stringBuilder.append("  Instance: ").append(instanceInfo).append("\n");
			}

			stringBuilder.append("\n");
		}
		return stringBuilder.toString();
	}

}
