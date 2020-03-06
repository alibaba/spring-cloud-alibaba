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

package com.alibaba.cloud.nacos.ribbon;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiaojing
 */
public class NacosRibbonClientConfigurationTests {

	private WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(NacosRibbonTestConfiguration.class,
					NacosRibbonClientConfiguration.class,
					NacosDiscoveryClientConfiguration.class,
					RibbonNacosAutoConfiguration.class))
			.withPropertyValues("spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848")
			.withPropertyValues("spring.cloud.nacos.discovery.port=18080")
			.withPropertyValues("spring.cloud.nacos.discovery.service=myapp");

	@Test
	public void testProperties() {

		this.contextRunner.run(context -> {
			NacosServerList serverList = context.getBean(NacosServerList.class);
			assertThat(serverList.getServiceId()).isEqualTo("myapp");
		});
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	static class NacosRibbonTestConfiguration {

		@Bean
		IClientConfig iClientConfig() {
			DefaultClientConfigImpl config = new DefaultClientConfigImpl();
			config.setClientName("myapp");
			return config;
		}

		@Bean
		@LoadBalanced
		RestTemplate restTemplate() {
			return new RestTemplate();
		}

	}

}
