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

package com.alibaba.cloud.nacos.loadbalancer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.ribbon.ExtendBalancer;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceSupplier;

/**
 * @author XuDaojie
 * @since 2.2.6
 */
public class NacosLoadBalancer implements ReactorServiceInstanceLoadBalancer {

	private static final Logger log = LoggerFactory.getLogger(NacosLoadBalancer.class);

	private final String serviceId;

	@Deprecated
	private ObjectProvider<ServiceInstanceSupplier> serviceInstanceSupplier;

	private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

	private final NacosDiscoveryProperties nacosDiscoveryProperties;

	private final NacosServiceManager nacosServiceManager;

	public NacosLoadBalancer(
			ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
			String serviceId, NacosDiscoveryProperties nacosDiscoveryProperties,
			NacosServiceManager nacosServiceManager) {
		this.serviceId = serviceId;
		this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
		this.nacosServiceManager = nacosServiceManager;
	}

	@Override
	public Mono<Response<ServiceInstance>> choose(Request request) {
		// Temporary conditional logic till deprecated members are removed.
		if (serviceInstanceListSupplierProvider != null) {
			ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
					.getIfAvailable(NoopServiceInstanceListSupplier::new);
			return supplier.get().next().map(this::getInstanceResponse);
		}
		ServiceInstanceSupplier supplier = this.serviceInstanceSupplier
				.getIfAvailable(NoopServiceInstanceSupplier::new);
		return supplier.get().collectList().map(this::getInstanceResponse);
	}

	private Response<ServiceInstance> getInstanceResponse(
			List<ServiceInstance> serviceInstances) {
		if (serviceInstances.isEmpty()) {
			log.warn("No servers available for service: " + this.serviceId);
			return new EmptyResponse();
		}

		try {
			String clusterName = this.nacosDiscoveryProperties.getClusterName();
			String group = this.nacosDiscoveryProperties.getGroup();
			String serviceName = serviceId;

			NamingService namingService = nacosServiceManager
					.getNamingService(nacosDiscoveryProperties.getNacosProperties());
			List<Instance> instances = namingService.selectInstances(serviceName, group,
					true);
			if (CollectionUtils.isEmpty(instances)) {
				log.warn("no instance in service {}", serviceName);
				return null;
			}

			List<Instance> instancesToChoose = instances;
			if (StringUtils.isNotBlank(clusterName)) {
				List<Instance> sameClusterInstances = instances.stream()
						.filter(instance -> Objects.equals(clusterName,
								instance.getClusterName()))
						.collect(Collectors.toList());
				if (!CollectionUtils.isEmpty(sameClusterInstances)) {
					instancesToChoose = sameClusterInstances;
				}
				else {
					log.warn(
							"A cross-cluster call occurs，name = {}, clusterName = {}, instance = {}",
							serviceName, clusterName, instances);
				}
			}

			Instance instance = ExtendBalancer.getHostByRandomWeight2(instancesToChoose);

			return new DefaultResponse(serviceInstances.stream()
					.filter(serviceInstance1 -> StringUtils.equals(instance.getIp(),
							serviceInstance1.getHost())
							&& instance.getPort() == serviceInstance1.getPort())
					.findFirst().get());
		}
		catch (Exception e) {
			log.warn("NacosLoadBalancer error", e);
			return null;
		}

	}

}
