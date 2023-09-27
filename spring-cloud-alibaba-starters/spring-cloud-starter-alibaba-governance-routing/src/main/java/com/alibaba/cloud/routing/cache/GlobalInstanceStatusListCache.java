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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
	 */
	private static final List<Map<String, List<Map<String, ServiceInstanceInfo>>>> globalServiceList = new ArrayList<>();

	/**
	 * Set instance cache.
	 * @param targetName service name.
	 * @param instance service instance object.
	 * @param sif service instance info.
	 */
	public static void set(String targetName, Instance instance,
			ServiceInstanceInfo sif) {

		if (globalServiceList.isEmpty()) {
			List<Map<String, ServiceInstanceInfo>> serviceInstanceList = new ArrayList<>();
			Map<String, ServiceInstanceInfo> instanceInfoMap = new HashMap<>();
			instanceInfoMap.put(instance.getIp() + ":" + instance.getPort(), sif);
			serviceInstanceList.add(instanceInfoMap);
			Map<String, List<Map<String, ServiceInstanceInfo>>> serviceMap = new ConcurrentHashMap<>();
			serviceMap.put(targetName, serviceInstanceList);
			globalServiceList.add(serviceMap);
		}
		else {
			for (Map<String, List<Map<String, ServiceInstanceInfo>>> serviceMap : globalServiceList) {
				for (String key : serviceMap.keySet()) {
					if (Objects.equals(key, targetName)) {

						List<Map<String, ServiceInstanceInfo>> serviceInstanceList = serviceMap
								.get(targetName);
						Map<String, ServiceInstanceInfo> infoHashMap = new HashMap<>();
						infoHashMap.put(instance.getIp() + ":" + instance.getPort(), sif);
						serviceInstanceList.add(infoHashMap);

						serviceMap.replace(targetName, serviceInstanceList);
					}
					else {
						List<Map<String, ServiceInstanceInfo>> serviceInstanceList = new ArrayList<>();
						Map<String, ServiceInstanceInfo> instanceInfoMap = new HashMap<>();
						instanceInfoMap.put(instance.getIp() + ":" + instance.getPort(), sif);
						serviceInstanceList.add(instanceInfoMap);
						Map<String, List<Map<String, ServiceInstanceInfo>>> serviceMapTmp = new HashMap<>();
						serviceMapTmp.put(targetName, serviceInstanceList);
						globalServiceList.add(serviceMapTmp);
					}
				}
			}
		}
	}

	/**
	 * Check instance in cache.
	 * @param target target.
	 * @param instance instance object.
	 * @return boolean
	 */
	public static boolean checkContainersInstance(String target, Instance instance) {
		AtomicBoolean res = new AtomicBoolean(false);

		globalServiceList.forEach(val -> val.forEach((var9, var10) -> {
			if (val.containsKey(target)) {
				var10.forEach(var1 -> {
					if (var1.containsKey(instance.getIp() + ":" + instance.getPort())) {

						res.set(true);
					}
				});
			}
		}));

		return res.get();
	}

	/**
	 * Return GlobalCache Object.
	 * @return return global instance cache
	 */
	public static List<Map<String, List<Map<String, ServiceInstanceInfo>>>> getAll() {

		return globalServiceList;
	}

	/**
	 * Get service instance list by service name.
	 * @param targetServiceName service name.
	 * @return instance list.
	 */
	public static List<Map<String, ServiceInstanceInfo>> getInstanceByServiceName(
			String targetServiceName) {

		for (Map<String, List<Map<String, ServiceInstanceInfo>>> map : globalServiceList) {
			if (map.containsKey(targetServiceName)) {
				return map.get(targetServiceName);
			}
		}

		return null;
	}

	/**
	 * Get instance info by global cache.
	 * @param instanceName instance name
	 * @return sif object
	 */
	public static ServiceInstanceInfo getInstanceByInstanceName(String instanceName) {

		for (Map<String, List<Map<String, ServiceInstanceInfo>>> map : globalServiceList) {
			for (String serviceName : map.keySet()) {
				for (Map<String, ServiceInstanceInfo> instanceInfoMap : map
						.get(serviceName)) {
					return instanceInfoMap.get(instanceName);
				}
			}
		}

		return null;
	}

	/**
	 * Get no health instance list.
	 * @return list
	 */
	public static List<ServiceInstanceInfo> getCalledErrorInstance() {

		List<ServiceInstanceInfo> res = new ArrayList<>();
		for (Map<String, List<Map<String, ServiceInstanceInfo>>> map : globalServiceList) {
			for (String serviceName : map.keySet()) {
				List<Map<String, ServiceInstanceInfo>> maps = map.get(serviceName);
				for (Map<String, ServiceInstanceInfo> instanceInfoMap : maps) {
					for (String instanceName : instanceInfoMap.keySet()) {
						ServiceInstanceInfo serviceInstanceInfo = instanceInfoMap
								.get(instanceName);
						if (Objects.nonNull(serviceInstanceInfo.getConsecutiveErrors())) {
							res.add(serviceInstanceInfo);
						}
					}
				}
			}
		}

		return res;
	}

	/**
	 * getServiceUpperLimitRatioNum.
	 * @param minHealthPercent minHealthPercent
	 * @return max remove instance num
	 */
	public static int getServiceUpperLimitRatioNum(String targetServiceName, double minHealthPercent) {

		int serviceInstanceTotal = 0;

		for (Map<String, List<Map<String, ServiceInstanceInfo>>> map : globalServiceList) {
			List<Map<String, ServiceInstanceInfo>> list = map.get(targetServiceName);
			serviceInstanceTotal = list.size();
		}

		return (int) Math.floor(serviceInstanceTotal * minHealthPercent);
	}

	/**
	 * Get all instance nums.
	 * @return remove instance num
	 */
	public static int getInstanceNumByTargetServiceName(String targetServiceName) {

		int serviceInstanceTotal = 0;

		for (Map<String, List<Map<String, ServiceInstanceInfo>>> map : globalServiceList) {
			List<Map<String, ServiceInstanceInfo>> maps = map.get(targetServiceName);
			serviceInstanceTotal = maps.size();
		}

		return serviceInstanceTotal;
	}

	/**
	 * Get no health nums.
	 * @return remove instance num
	 */
	public static int getRemoveInstanceNum(String targetServiceName) {

		int serviceInstanceTotal = 0;

		for (Map<String, List<Map<String, ServiceInstanceInfo>>> map : globalServiceList) {
			List<Map<String, ServiceInstanceInfo>> maps = map.get(targetServiceName);
			for (Map<String, ServiceInstanceInfo> instanceInfoMap : maps) {
				for (String val : instanceInfoMap.keySet()) {
					ServiceInstanceInfo serviceInstanceInfo = instanceInfoMap.get(val);
					if (!(serviceInstanceInfo.isStatus())) {
						serviceInstanceTotal ++;
					}
				}
			}
		}

		return serviceInstanceTotal;
	}

	public static void setInstanceInfoByInstanceNames(ServiceInstanceInfo sif) {

		String instanceName = sif.getInstance().getIp() + ":" + sif.getInstance().getPort();
		for (Map<String, List<Map<String, ServiceInstanceInfo>>> maps : globalServiceList) {
			for (String key : maps.keySet()) {
				List<Map<String, ServiceInstanceInfo>> list = maps.get(key);
				for (Map<String, ServiceInstanceInfo> instanceInfoMap : list) {
					instanceInfoMap.replace(instanceName, sif);
				}
			}
		}

	}

	/**
	 * ToString method.
	 * @return str
	 */
	@Override
	public String toString() {
		StringBuilder var16 = new StringBuilder();
		var16.append("{\n");
		for (Map<String, List<Map<String, ServiceInstanceInfo>>> map : globalServiceList) {
			var16.append("  ").append(map.keySet().iterator().next()).append(": [\n");
			for (Map<String, ServiceInstanceInfo> innerMap : map
					.get(map.keySet().iterator().next())) {
				var16.append("    ").append(innerMap.keySet().iterator().next())
						.append(": ")
						.append(innerMap.get(innerMap.keySet().iterator().next()))
						.append("\n");
			}
			var16.append("  ").append("],\n");
		}
		var16.append("}");

		return var16.toString();
	}

}
