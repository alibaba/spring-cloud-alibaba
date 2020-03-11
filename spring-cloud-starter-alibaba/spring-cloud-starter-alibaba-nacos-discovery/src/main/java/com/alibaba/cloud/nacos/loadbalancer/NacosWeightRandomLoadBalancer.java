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
import java.util.stream.Collectors;

import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.nacos.client.naming.utils.Chooser;
import com.alibaba.nacos.client.naming.utils.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

/**
 * A Weight-based implementation of {@link ReactorServiceInstanceLoadBalancer}.
 *
 * @author sivan757
 */
public class NacosWeightRandomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

	private static final Log log = LogFactory.getLog(NacosWeightRandomLoadBalancer.class);

	private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

	private final String serviceId;

	/**
	 * @param serviceInstanceListSupplierProvider a provider of
	 * {@link ServiceInstanceListSupplier} that will be used to get available instances
	 * @param serviceId id of the service for which to choose an instance
	 */
	public NacosWeightRandomLoadBalancer(
			ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
			String serviceId) {
		this.serviceId = serviceId;
		this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
	}

	@Override
	public Mono<Response<ServiceInstance>> choose(Request request) {
		ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
				.getIfAvailable(NoopServiceInstanceListSupplier::new);
		return supplier.get().next().map(this::getInstanceResponse);
	}

	private Response<ServiceInstance> getInstanceResponse(
			List<ServiceInstance> instances) {
		if (instances.isEmpty()) {
			log.warn("No servers available for service: " + this.serviceId);
			return new EmptyResponse();
		}

		ServiceInstance instance = getHostByRandomWeight(instances);

		return new DefaultResponse(instance);
	}

	/**
	 * Return one {@link ServiceInstance} from the host list by random-weight.
	 * @param serviceInstances The list of the instance.
	 * @return The random-weight result of the instance.
	 *
	 * @see com.alibaba.nacos.client.naming.core.Balancer#getHostByRandomWeight
	 */
	protected ServiceInstance getHostByRandomWeight(
			List<ServiceInstance> serviceInstances) {
		log.debug("entry randomWithWeight");
		if (serviceInstances == null || serviceInstances.size() == 0) {
			log.debug("serviceInstances == null || serviceInstances.size() == 0");
			return null;
		}

		Chooser<String, ServiceInstance> instanceChooser = new Chooser<>(
				"com.alibaba.nacos");

		List<Pair<ServiceInstance>> hostsWithWeight = serviceInstances.stream()
				.map(serviceInstance -> new Pair<>(serviceInstance,
						getWeight(serviceInstance)))
				.collect(Collectors.toList());

		instanceChooser.refresh(hostsWithWeight);
		log.debug("refresh instanceChooser");
		return instanceChooser.randomWithWeight();
	}

	/**
	 * Get {@link ServiceInstance} weight metadata.
	 * @param serviceInstance instance
	 * @return The weight of the instance.
	 *
	 * @see NacosServiceDiscovery#hostToServiceInstance
	 */
	protected double getWeight(ServiceInstance serviceInstance) {
		return Double.parseDouble(serviceInstance.getMetadata().get("nacos.weight"));
	}

}
