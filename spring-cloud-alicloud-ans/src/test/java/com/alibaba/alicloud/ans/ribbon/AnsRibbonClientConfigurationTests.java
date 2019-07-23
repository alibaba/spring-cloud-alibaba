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

package com.alibaba.alicloud.ans.ribbon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.alibaba.alicloud.ans.AnsAutoConfiguration;
import com.alibaba.alicloud.ans.AnsDiscoveryClientAutoConfiguration;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;

/**
 * @author xiaojing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnsRibbonClientConfigurationTests.TestConfig.class, properties = {
		"spring.application.name=myTestService1",
		"spring.cloud.alicloud.ans.server-list=127.0.0.1",
		"spring.cloud.alicloud.ans.server-port=8080",
		"spring.cloud.alicloud.ans.endpoint=test-endpoint" }, webEnvironment = RANDOM_PORT)
public class AnsRibbonClientConfigurationTests {

	@Autowired
	private AnsServerList serverList;

	@Test
	public void contextLoads() throws Exception {
		assertThat(serverList.getDom()).isEqualTo("myapp");
	}

	@Configuration
	public static class AnsRibbonTestConfiguration {

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

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			AnsDiscoveryClientAutoConfiguration.class, AnsAutoConfiguration.class,
			AnsRibbonTestConfiguration.class, RibbonAnsAutoConfiguration.class,
			AnsRibbonClientConfiguration.class })
	public static class TestConfig {
	}

}
