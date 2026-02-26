/*
 * Copyright 2013-present the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.util.InetIPv6Utils;
import com.alibaba.nacos.common.utils.CollectionUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

/**
 * see original.
 * {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer}
 *
 * @author XuDaojie
 * @since 2021.1
 */
public class NacosLoadBalancer implements ReactorServiceInstanceLoadBalancer {

	private static final Logger log = LoggerFactory.getLogger(NacosLoadBalancer.class);

	private final String serviceId;

	private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

	private final NacosDiscoveryProperties nacosDiscoveryProperties;

	private static final String IPV4_REGEX = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";

	private static final String IPV6_KEY = "IPv6";
	/**
	 * Storage local valid IPv6 address, it's a flag whether local machine support IPv6 address stack.
	 */
	public static String ipv6 = "";

	private final InetIPv6Utils inetIPv6Utils;

	private final List<ServiceInstanceFilter> serviceInstanceFilters;

	private final Map<String, LoadBalancerAlgorithm> loadBalancerAlgorithmMap;

	@PostConstruct
	public void init() {
		String ip = nacosDiscoveryProperties.getIp();
		if (StringUtils.isNotEmpty(ip)) {
			String resolvedIp = Pattern.matches(IPV4_REGEX, ip)
					? nacosDiscoveryProperties.getMetadata().get(IPV6_KEY) : ip;
			ipv6 = resolvedIp == null ? "" : resolvedIp;
		}
		else {
			String found = inetIPv6Utils.findIPv6Address();
			ipv6 = found == null ? "" : found;
		}
	}

	private List<ServiceInstance> filterInstanceByIpType(List<ServiceInstance> instances) {
		if (StringUtils.isNotEmpty(ipv6)) {
			List<ServiceInstance> ipv6InstanceList = new ArrayList<>();
			for (ServiceInstance instance : instances) {
				Map<String, String> metadata = instance.getMetadata();
				if (Pattern.matches(IPV4_REGEX, instance.getHost())) {
					String ipv6Metadata = metadata == null ? null : metadata.get(IPV6_KEY);
					if (StringUtils.isNotEmpty(ipv6Metadata)) {
						ipv6InstanceList.add(instance);
					}
				}
				else {
					ipv6InstanceList.add(instance);
				}
			}
			// Provider has no IPv6, should use IPv4.
			if (ipv6InstanceList.isEmpty()) {
				return instances.stream()
						.filter(instance -> Pattern.matches(IPV4_REGEX, instance.getHost()))
						.collect(Collectors.toList());
			}
			else {
				return ipv6InstanceList;
			}
		}
		return instances.stream()
				.filter(instance -> Pattern.matches(IPV4_REGEX, instance.getHost()))
				.collect(Collectors.toList());
	}

	public NacosLoadBalancer(
			ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
			String serviceId, NacosDiscoveryProperties nacosDiscoveryProperties, InetIPv6Utils inetIPv6Utils,
			List<ServiceInstanceFilter> serviceInstanceFilters,
			Map<String, LoadBalancerAlgorithm> loadBalancerAlgorithmMap) {
		this.serviceId = serviceId;
		this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
		this.inetIPv6Utils = inetIPv6Utils;
		this.serviceInstanceFilters = serviceInstanceFilters;
		this.loadBalancerAlgorithmMap = loadBalancerAlgorithmMap;
	}

	@Override
	public Mono<Response<ServiceInstance>> choose(Request request) {
		ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
				.getIfAvailable(NoopServiceInstanceListSupplier::new);
		return supplier.get(request).next().map(serviceInstances -> getInstanceResponse(request, serviceInstances));
	}

	private Response<ServiceInstance> getInstanceResponse(Request<?> request,
			List<ServiceInstance> serviceInstances) {
		if (serviceInstances.isEmpty()) {
			log.warn("No servers available for service: {}", this.serviceId);
			return new EmptyResponse();
		}

		try {
			String clusterName = this.nacosDiscoveryProperties.getClusterName();

			List<ServiceInstance> instancesToChoose = serviceInstances;
			if (clusterName != null && StringUtils.isNotBlank(clusterName)) {
				List<ServiceInstance> sameClusterInstances = serviceInstances.stream()
						.filter(serviceInstance -> {
							Map<String, String> metadata = serviceInstance.getMetadata();
							String cluster = metadata == null ? null : metadata.get("nacos.cluster");
							return Objects.equals(cluster, clusterName);
						}).collect(Collectors.toList());
				if (!CollectionUtils.isEmpty(sameClusterInstances)) {
					instancesToChoose = sameClusterInstances;
				}
			}
			else {
				log.warn(
						"A cross-cluster call occurs，name = {}, clusterName = {}, instance = {}",
						serviceId, clusterName, serviceInstances);
			}
			instancesToChoose = this.filterInstanceByIpType(instancesToChoose);

			// Filter the service list sequentially based on the order number
			for (ServiceInstanceFilter filter : serviceInstanceFilters) {
				instancesToChoose = filter.filterInstance(request, instancesToChoose);
			}

			ServiceInstance instance;
			// Find the corresponding load balancing algorithm through the service ID and select the final service instance
			if (loadBalancerAlgorithmMap.containsKey(serviceId)) {
				LoadBalancerAlgorithm loadBalancerAlgorithm = loadBalancerAlgorithmMap
						.get(serviceId);
				if (loadBalancerAlgorithm == null) {
					return new EmptyResponse();
				}
				instance = loadBalancerAlgorithm.getInstance(request, instancesToChoose);
			}
			else {
				LoadBalancerAlgorithm defaultLoadBalancerAlgorithm = loadBalancerAlgorithmMap
						.get(LoadBalancerAlgorithm.DEFAULT_SERVICE_ID);
				if (defaultLoadBalancerAlgorithm == null) {
					return new EmptyResponse();
				}
				instance = defaultLoadBalancerAlgorithm.getInstance(request, instancesToChoose);
			}
			if (instance == null) {
				return new EmptyResponse();
			}

			return new DefaultResponse(instance);
		}
		catch (Exception e) {
			log.warn("NacosLoadBalancer error", e);
			return new EmptyResponse();
		}
	}

}
