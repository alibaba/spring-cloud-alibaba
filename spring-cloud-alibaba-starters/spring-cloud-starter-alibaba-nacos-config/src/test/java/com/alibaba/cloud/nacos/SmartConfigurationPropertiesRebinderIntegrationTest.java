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

package com.alibaba.cloud.nacos;

import com.alibaba.cloud.nacos.refresh.RefreshBehavior;
import com.alibaba.cloud.nacos.refresh.SmartConfigurationPropertiesRebinder;
import org.junit.jupiter.api.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SmartConfigurationPropertiesRebinder} tester.
 *
 * @author freeman
 */
public class SmartConfigurationPropertiesRebinderIntegrationTest {

	ConfigurableApplicationContext context;

	@Test
	public void testUsingSmartConfigurationPropertiesRebinder_whenBehaviorIsNotDefault() {
		context = new SpringApplicationBuilder(RebinderConfiguration.class)
				.web(WebApplicationType.NONE)
				.properties("spring.cloud.nacos.config.refresh-behavior=specific_bean")
				.properties("spring.cloud.nacos.server-addr=123.123.123.123:8848")
				.properties("spring.cloud.nacos.config.import-check.enabled=false").run();

		ConfigurationPropertiesRebinder rebinder = context
				.getBean(ConfigurationPropertiesRebinder.class);

		assertThat(rebinder.getClass())
				.isEqualTo(SmartConfigurationPropertiesRebinder.class);

		RefreshBehavior refreshBehavior = (RefreshBehavior) ReflectionTestUtils
				.getField(rebinder, "refreshBehavior");
		assertThat(refreshBehavior).isEqualTo(RefreshBehavior.SPECIFIC_BEAN);
	}

	@Test
	public void testUsingConfigurationPropertiesRebinder_whenBehaviorIsDefault() {
		context = new SpringApplicationBuilder(RebinderConfiguration.class)
				.web(WebApplicationType.NONE)
				.properties("spring.cloud.nacos.server-addr=123.123.123.123:8848")
				.properties("spring.cloud.nacos.config.import-check.enabled=false").run();

		ConfigurationPropertiesRebinder rebinder = context
				.getBean(ConfigurationPropertiesRebinder.class);

		assertThat(rebinder.getClass()).isEqualTo(ConfigurationPropertiesRebinder.class);
	}

	@Configuration
	@ImportAutoConfiguration({ NacosConfigAutoConfiguration.class })
	@EnableAutoConfiguration
	public static class RebinderConfiguration {

	}

}
