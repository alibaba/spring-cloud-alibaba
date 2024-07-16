/*
 * Copyright 2023-2024 the original author or authors.
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

package com.alibaba.cloud.nacos.discovery;

import com.alibaba.cloud.nacos.NacosServiceAutoConfiguration;
import com.alibaba.cloud.nacos.loadbalancer.LoadBalancerAlgorithm;
import com.alibaba.cloud.nacos.loadbalancer.LoadBalancerNacosAutoConfiguration;
import com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancerClientConfiguration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration;
import com.alibaba.cloud.nacos.util.UtilIPv6AutoConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:zhangbin1010@qq.com">zhangbinhub</a>
 **/
public class NacosDiscoveryLoadBalancerConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(
					AutoServiceRegistrationConfiguration.class,
					NacosServiceRegistryAutoConfiguration.class,
					UtilAutoConfiguration.class,
					UtilIPv6AutoConfiguration.class,
					NacosServiceAutoConfiguration.class,
					NacosDiscoveryAutoConfiguration.class,
					NacosDiscoveryClientConfiguration.class,
					LoadBalancerAutoConfiguration.class, this.getClass()));

	@Test
	public void testNacosLoadBalancerEnabled() {
		contextRunner.withPropertyValues("spring.cloud.loadbalancer.nacos.enabled=true")
				.withConfiguration(AutoConfigurations.of(
						LoadBalancerNacosAutoConfiguration.class,
						NacosLoadBalancerClientConfiguration.class))
				.run(context -> {
					assertThat(context).hasSingleBean(LoadBalancerAlgorithm.class);
					assertThat(context).hasBean("nacosLoadBalancer");
				});
	}

	@Test
	public void testNacosLoadBalancerDisabled() {
		contextRunner.withPropertyValues("spring.cloud.loadbalancer.nacos.enabled=false")
				.withConfiguration(AutoConfigurations.of(
						LoadBalancerNacosAutoConfiguration.class,
						NacosLoadBalancerClientConfiguration.class))
				.run(context -> {
					assertThat(context).doesNotHaveBean(LoadBalancerAlgorithm.class);
					assertThat(context).doesNotHaveBean("nacosLoadBalancer");
				});
	}

}
