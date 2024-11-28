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

package com.alibaba.cloud.nacos.loadbalancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.util.InetIPv6Utils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

/**
 * {@link ServiceInstanceListSupplier} don't use cache.<br>
 * <br>
 * 1. LoadBalancerCache causes information such as the weight of the service instance to
 * be changed without immediate effect.<br>
 * 2. Nacos itself supports caching.
 *
 * @author XuDaojie
 * @since 2021.1
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnLoadBalancerNacos
@ConditionalOnDiscoveryEnabled
public class NacosLoadBalancerClientConfiguration {

	private static final int REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER = 183827465;

	@Bean
	@ConditionalOnMissingBean
	public ReactorLoadBalancer<ServiceInstance> nacosLoadBalancer(Environment environment,
			LoadBalancerClientFactory loadBalancerClientFactory,
			NacosDiscoveryProperties nacosDiscoveryProperties,
			InetIPv6Utils inetIPv6Utils,
			List<ServiceInstanceFilter> serviceInstanceFilters,
			List<LoadBalancerAlgorithm> loadBalancerAlgorithms) {
		String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
		Map<String, LoadBalancerAlgorithm> loadBalancerAlgorithmMap = new HashMap<>();
		loadBalancerAlgorithms.forEach(loadBalancerAlgorithm -> {
			if (!loadBalancerAlgorithmMap.containsKey(loadBalancerAlgorithm.getServiceId())) {
				loadBalancerAlgorithmMap.put(loadBalancerAlgorithm.getServiceId(), loadBalancerAlgorithm);
			}
		});
		return new NacosLoadBalancer(
				loadBalancerClientFactory.getLazyProvider(name,
						ServiceInstanceListSupplier.class),
				name, nacosDiscoveryProperties, inetIPv6Utils,
				serviceInstanceFilters, loadBalancerAlgorithmMap);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnReactiveDiscoveryEnabled
	@Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER)
	public static class ReactiveSupportConfiguration {

		@Bean
		@ConditionalOnBean(ReactiveDiscoveryClient.class)
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "default", matchIfMissing = true)
		public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
				ConfigurableApplicationContext context) {
			return ServiceInstanceListSupplier.builder().withDiscoveryClient()
					.build(context);
		}

		@Bean
		@ConditionalOnBean(ReactiveDiscoveryClient.class)
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "zone-preference")
		public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(
				ConfigurableApplicationContext context) {
			return ServiceInstanceListSupplier.builder().withDiscoveryClient()
					.withZonePreference().build(context);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBlockingDiscoveryEnabled
	@Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER + 1)
	public static class BlockingSupportConfiguration {

		@Bean
		@ConditionalOnBean(DiscoveryClient.class)
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "default", matchIfMissing = true)
		public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
				ConfigurableApplicationContext context) {
			return ServiceInstanceListSupplier.builder().withBlockingDiscoveryClient()
					.build(context);
		}

		@Bean
		@ConditionalOnBean(DiscoveryClient.class)
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "zone-preference")
		public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(
				ConfigurableApplicationContext context) {
			return ServiceInstanceListSupplier.builder().withBlockingDiscoveryClient()
					.withZonePreference().build(context);
		}
	}
}
