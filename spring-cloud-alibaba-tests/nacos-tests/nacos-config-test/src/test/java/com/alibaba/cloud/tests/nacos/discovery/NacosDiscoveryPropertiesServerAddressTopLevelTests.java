/*
 * Copyright 2013-2022 the original author or authors.
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

package com.alibaba.cloud.tests.nacos.discovery;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = NacosDiscoveryPropertiesServerAddressBothLevelTests.TestConfig.class, properties = {
		"spring.application.name=app", "spring.cloud.nacos.server-addr=127.0.0.1:8848" })
public class NacosDiscoveryPropertiesServerAddressTopLevelTests {

	@Autowired
	private NacosDiscoveryProperties properties;

	@Test
	public void testGetServerAddr() {
		assertThat(properties.getServerAddr()).isEqualTo("127.0.0.1:8848");
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			NacosDiscoveryClientConfiguration.class,
			NacosServiceRegistryAutoConfiguration.class })
	public static class TestConfig {

	}
}