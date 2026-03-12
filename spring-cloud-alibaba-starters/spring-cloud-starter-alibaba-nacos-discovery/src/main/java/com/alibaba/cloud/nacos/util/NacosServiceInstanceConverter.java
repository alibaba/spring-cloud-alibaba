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

package com.alibaba.cloud.nacos.util;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.alibaba.nacos.api.naming.pojo.Instance;

import org.springframework.cloud.client.ServiceInstance;



/**
 * @author Karson
 */
final public class NacosServiceInstanceConverter {

		private static final String WEIGHT = "nacos.weight";

		private static final String HEALTHY = "nacos.healthy";

		private NacosServiceInstanceConverter() { }

		public static ServiceInstance fromInstanceAndServiceId(Instance instance, String serviceId) {
				return ServiceInstanceBuilder.fromInstanceAndServiceId(instance, serviceId).build();
		}

		public static Instance fromServiceInstance(ServiceInstance instance) {
				return InstanceServiceBuilder.fromServiceInstance(instance).build();
		}

		interface Builder<T> {

				T build();
		}

		final static class InstanceServiceBuilder implements Builder<Instance> {

				private ServiceInstance serviceInstance;

				private InstanceServiceBuilder() {

				}

				private void setServiceInstance(ServiceInstance serviceInstance) {
						this.serviceInstance = serviceInstance;
				}

				private static InstanceServiceBuilder fromServiceInstance(ServiceInstance instance) {
						InstanceServiceBuilder instanceServiceBuilder = new InstanceServiceBuilder();
						instanceServiceBuilder.setServiceInstance(instance);
						return instanceServiceBuilder;
				}

				@Override
				public Instance build() {
						Instance instance = new Instance();
						Map<String, String> metadata = serviceInstance.getMetadata();
						instance.setIp(serviceInstance.getHost());
						instance.setPort(serviceInstance.getPort());
						instance.setWeight(Double.parseDouble(metadata.get(WEIGHT)));
						instance.setHealthy(Boolean.parseBoolean(metadata.get(HEALTHY)));
						return instance;
				}
		}

		final static class ServiceInstanceBuilder implements Builder<ServiceInstance> {

				private Instance instance;
				private String serviceId;

				private ServiceInstanceBuilder() {
				}

				private static ServiceInstanceBuilder fromInstanceAndServiceId(Instance instance, String serviceId) {
						ServiceInstanceBuilder nacosServiceInstanceBuilder = new ServiceInstanceBuilder();
						nacosServiceInstanceBuilder.setInstance(instance);
						nacosServiceInstanceBuilder.setServiceId(serviceId);
						return nacosServiceInstanceBuilder;
				}

				private void setInstance(Instance instance) {
						this.instance = instance;
				}

				private void setServiceId(String serviceId) {
						this.serviceId = serviceId;
				}

				@Override
				public ServiceInstance build() {
						if (this.instance == null || !this.instance.isEnabled() || !this.instance.isHealthy()) {
								return null;
						}
						NacosServiceInstance nacosServiceInstance = new NacosServiceInstance();
						nacosServiceInstance.setHost(instance.getIp());
						nacosServiceInstance.setPort(instance.getPort());
						nacosServiceInstance.setServiceId(serviceId);
						nacosServiceInstance.setInstanceId(instance.getInstanceId());

						Map<String, String> metadata = new HashMap<>();
						metadata.put("nacos.instanceId", instance.getInstanceId());
						metadata.put(WEIGHT, instance.getWeight() + "");
						metadata.put(HEALTHY, instance.isHealthy() + "");
						metadata.put("nacos.cluster", instance.getClusterName() + "");
						if (instance.getMetadata() != null) {
								metadata.putAll(instance.getMetadata());
						}
						metadata.put("nacos.ephemeral", String.valueOf(instance.isEphemeral()));
						nacosServiceInstance.setMetadata(metadata);

						if (metadata.containsKey("secure")) {
								boolean secure = Boolean.parseBoolean(metadata.get("secure"));
								nacosServiceInstance.setSecure(secure);
						}
						return nacosServiceInstance;
				}
		}

}
