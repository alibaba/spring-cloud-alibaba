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

package com.alibaba.cloud.nacos.balancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancer;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;

import org.springframework.cloud.client.ServiceInstance;

/**
 * @author itmuch.com XuDaojie
 * @since 2021.1
 */
public class NacosBalancer extends Balancer {

	private static final String IPV4_REGEX = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";

	private static final String IPV6_KEY = "IPv6";

	/**
	 * Choose instance by weight.
	 * @param instances Instance List
	 * @return the chosen instance
	 */
	public static Instance getHostByRandomWeight2(List<Instance> instances) {
		return getHostByRandomWeight(instances);
	}

	/**
	 * Spring Cloud LoadBalancer Choose instance by weight.
	 * @param serviceInstances Instance List
	 * @return the chosen instance
	 */
	public static ServiceInstance getHostByRandomWeight3(
			List<ServiceInstance> serviceInstances) {
		Map<Instance, ServiceInstance> instanceMap = new HashMap<>();
		List<Instance> nacosInstance = serviceInstances.stream().map(serviceInstance -> {
			Map<String, String> metadata = serviceInstance.getMetadata();

			// see
			// com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery.hostToServiceInstance()
			Instance instance = new Instance();
			instance.setIp(serviceInstance.getHost());
			instance.setPort(serviceInstance.getPort());
			instance.setWeight(Double.parseDouble(metadata.get("nacos.weight")));
			instance.setHealthy(Boolean.parseBoolean(metadata.get("nacos.healthy")));
			instanceMap.put(instance, serviceInstance);
			return instance;
		}).collect(Collectors.toList());

		Instance instance = getHostByRandomWeight2(nacosInstance);
		NacosServiceInstance nacosServiceInstance = (NacosServiceInstance) instanceMap.get(instance);
		// When local support IPv6 address stack, referred to use IPv6 address.
		if (StringUtils.isNotEmpty(NacosLoadBalancer.ipv6)) {
			convertIPv4ToIPv6(nacosServiceInstance);
		}
		return nacosServiceInstance;
	}

	/**
	 * There is two type Ip,using IPv6 should use IPv6 in metadata to replace IPv4 in IP
	 * field.
	 */
	private static void convertIPv4ToIPv6(NacosServiceInstance instance) {
		if (Pattern.matches(IPV4_REGEX, instance.getHost())) {
			String ip = instance.getMetadata().get(IPV6_KEY);
			if (StringUtils.isNotEmpty(ip)) {
				instance.setHost(ip);
			}
		}
	}

}
