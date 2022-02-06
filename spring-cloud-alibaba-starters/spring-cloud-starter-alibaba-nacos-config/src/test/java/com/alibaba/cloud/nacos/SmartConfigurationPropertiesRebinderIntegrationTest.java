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

package com.alibaba.cloud.nacos;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.cloud.nacos.refresh.RefreshBehavior;
import com.alibaba.cloud.nacos.refresh.SmartConfigurationPropertiesRebinder;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import static com.alibaba.cloud.nacos.SmartConfigurationPropertiesRebinderIntegrationTest.TestConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 *
 *
 * @author freeman
 * @date 2022/2/6
 */
@SpringBootTest(classes = TestConfig.class, properties = {
		"spring.cloud.nacos.config.refresh-behavior=specific",
		"spring.cloud.nacos.server-addr=123.123.123.123:8848" }, webEnvironment = RANDOM_PORT)
public class SmartConfigurationPropertiesRebinderIntegrationTest {

	@Autowired
	private ConfigurationPropertiesRebinder rebinder;
	@Autowired
	private ApplicationContext context;

	@Test
	public void testUsingSmartConfigurationPropertiesRebinder() {
		assertThat(rebinder.getClass())
				.isEqualTo(SmartConfigurationPropertiesRebinder.class);

		RefreshBehavior refreshBehavior = (RefreshBehavior) ReflectionTestUtils
				.getField(rebinder, "refreshBehavior");
		assertThat(refreshBehavior).isEqualTo(RefreshBehavior.SPECIFIC);
	}

	@Test
	public void testSpecificRefreshWork() {
		Set<String> keys = new HashSet<>();
		keys.add("spring.cloud.nacos.config.server-addr");
		keys.add("spring.cloud.nacos.config.name");

		// for debug
		context.publishEvent(new EnvironmentChangeEvent(context, keys));
	}

	@Configuration
	@ImportAutoConfiguration({ NacosConfigAutoConfiguration.class })
	@EnableAutoConfiguration
	public static class TestConfig {

	}

}
