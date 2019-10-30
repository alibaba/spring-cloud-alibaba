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

package com.alibaba.cloud.nacos;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientAutoConfiguration;
import com.alibaba.cloud.nacos.registry.NacosRegistration;

import org.junit.Before;
import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiaojing
 */
public class NacosDiscoveryAutoConfigurationTests {

	private ConfigurableApplicationContext context;

	@Before
	public void setUp() throws Exception {
		this.context = new SpringApplicationBuilder(NacosDiscoveryTestConfiguration.class,
				NacosDiscoveryClientAutoConfiguration.class,
				NacosDiscoveryAutoConfiguration.class).web(false).run(
						"--spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848",
						"--spring.cloud.nacos.discovery.port=18080",
						"--spring.cloud.nacos.discovery.service=myapp");
	}

	@Test
	public void testProperties() {

		NacosDiscoveryProperties properties = context
				.getBean(NacosDiscoveryProperties.class);
		assertThat(properties.getPort()).isEqualTo(18080);
		assertThat(properties.getServerAddr()).isEqualTo("127.0.0.1:8848");
		assertThat(properties.getService()).isEqualTo("myapp");

	}

	@Test
	public void nacosRegistration() {

		NacosRegistration nacosRegistration = context.getBean(NacosRegistration.class);
		assertThat(nacosRegistration.getPort()).isEqualTo(18080);
		assertThat(nacosRegistration.getServiceId()).isEqualTo("myapp");
		assertThat(nacosRegistration.getRegisterWeight()).isEqualTo(1F);

	}

	@Configuration
	@AutoConfigureBefore(NacosDiscoveryAutoConfiguration.class)
	static class NacosDiscoveryTestConfiguration {

		@Bean
		@ConditionalOnMissingBean
		AutoServiceRegistrationProperties autoServiceRegistrationProperties() {
			return new AutoServiceRegistrationProperties();
		}

		@Bean
		InetUtils inetUtils() {
			return new InetUtils(new InetUtilsProperties());
		}
	}

}