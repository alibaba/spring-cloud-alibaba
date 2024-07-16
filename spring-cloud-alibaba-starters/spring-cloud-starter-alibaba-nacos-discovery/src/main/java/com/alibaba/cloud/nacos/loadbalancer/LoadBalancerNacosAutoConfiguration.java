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

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} that sets up LoadBalancer for Nacos.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnLoadBalancerNacos
@ConditionalOnNacosDiscoveryEnabled
@LoadBalancerClients(defaultConfiguration = NacosLoadBalancerClientConfiguration.class)
public class LoadBalancerNacosAutoConfiguration {
	@Bean
	public LoadBalancerAlgorithm defaultLoadBalancerAlgorithm() {
		return new DefaultLoadBalancerAlgorithm();
	}
}
