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

package org.springframework.cloud.alibaba.nacos;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.cloud.alibaba.nacos.registry.NacosRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiaojing
 */
public class NacosDiscoveryAutoConfigurationTests {

	private WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(
					AutoConfigurations.of(NacosDiscoveryTestConfiguration.class,
							NacosDiscoveryAutoConfiguration.class,
							NacosDiscoveryClientAutoConfiguration.class))
			.withPropertyValues("spring.cloud.nacos.discovery.server-addr=127.0.0.1:8080")
			.withPropertyValues("spring.cloud.nacos.discovery.port=18080")
			.withPropertyValues("spring.cloud.nacos.discovery.service=myapp");

	@Test
	public void testProperties() {
		this.contextRunner.run(context -> {
			NacosDiscoveryProperties properties = context
					.getBean(NacosDiscoveryProperties.class);
			assertThat(properties.getPort()).isEqualTo(18080);
			assertThat(properties.getServerAddr()).isEqualTo("127.0.0.1:8080");
			assertThat(properties.getService()).isEqualTo("myapp");
		});
	}

	@Test
	public void nacosRegistration() {
		this.contextRunner.run(context -> {
			NacosRegistration nacosRegistration = context
					.getBean(NacosRegistration.class);
			assertThat(nacosRegistration.getPort()).isEqualTo(18080);
			assertThat(nacosRegistration.getServiceId()).isEqualTo("myapp");
			assertThat(nacosRegistration.getRegisterWeight()).isEqualTo(1F);
		});
	}

	@Configuration
	@AutoConfigureBefore(NacosDiscoveryAutoConfiguration.class)
	static class NacosDiscoveryTestConfiguration {

		@Bean
		AutoServiceRegistrationProperties autoServiceRegistrationProperties() {
			return new AutoServiceRegistrationProperties();
		}

		@Bean
		InetUtils inetUtils() {
			return new InetUtils(new InetUtilsProperties());
		}
	}

}