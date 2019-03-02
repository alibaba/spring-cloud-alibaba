/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.nacos.discovery;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.cloud.alibaba.nacos.NacosServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.*;

/**
 * @author xiaojing
 * @author renhaojun
 */
public class NacosDiscoveryClient implements DiscoveryClient {

	private static final Logger log = LoggerFactory
			.getLogger(NacosDiscoveryClient.class);
	public static final String DESCRIPTION = "Spring Cloud Nacos Discovery Client";

	private NacosDiscoveryProperties discoveryProperties;

	public NacosDiscoveryClient(NacosDiscoveryProperties discoveryProperties) {
		this.discoveryProperties = discoveryProperties;
	}

	@Override
	public String description() {
		return DESCRIPTION;
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		try {
			List<Instance> instances = discoveryProperties.namingServiceInstance()
					.selectInstances(serviceId, true);
			return hostToServiceInstanceList(instances, serviceId);
		}
		catch (Exception e) {
			throw new RuntimeException(
					"Can not get hosts from nacos server. serviceId: " + serviceId, e);
		}
	}

	private static ServiceInstance hostToServiceInstance(Instance instance,
			String serviceId) {
		NacosServiceInstance nacosServiceInstance = new NacosServiceInstance();
		nacosServiceInstance.setHost(instance.getIp());
		nacosServiceInstance.setPort(instance.getPort());
		nacosServiceInstance.setServiceId(serviceId);
		Map<String, String> metadata = new HashMap<>();
		metadata.put("instanceId", instance.getInstanceId());
		metadata.put("weight", instance.getWeight() + "");
		metadata.put("healthy", instance.isHealthy() + "");
		metadata.put("cluster", instance.getClusterName() + "");
		metadata.putAll(instance.getMetadata());
		nacosServiceInstance.setMetadata(metadata);

		if (metadata.containsKey("secure")) {
			boolean secure = Boolean.parseBoolean(metadata.get("secure"));
			nacosServiceInstance.setSecure(secure);
		}
		return nacosServiceInstance;
	}

	private static List<ServiceInstance> hostToServiceInstanceList(
			List<Instance> instances, String serviceId) {
		List<ServiceInstance> result = new ArrayList<>(instances.size());
		for (Instance instance : instances) {
			result.add(hostToServiceInstance(instance, serviceId));
		}
		return result;
	}

	@Override
	public List<String> getServices() {

		try {
			ListView<String> services = discoveryProperties.namingServiceInstance()
					.getServicesOfServer(1, Integer.MAX_VALUE);
			return services.getData();
		}
		catch (Exception e) {
			log.error("get service name from nacos server fail,", e);
			return Collections.emptyList();
		}
	}
}
