/*
 * Copyright 2013-present the original author or authors.
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

package com.alibaba.cloud.sentinel.gateway;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GatewayEnvironmentPostProcessorTest {
	/**
	 * Tests the custom property source processing logic.
	 * This test case verifies whether the custom property source is correctly added during the environment post-processing.
	 * Specifically, it checks if the configuration property "spring.cloud.sentinel.filter.enabled" is properly added.
	 */
	@Test
	public void testPostProcessEnvironment() {
		ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
		MutablePropertySources propertySources = new MutablePropertySources();
		when(environment.getPropertySources()).thenReturn(propertySources);

		GatewayEnvironmentPostProcessor postProcessor = new GatewayEnvironmentPostProcessor();
		postProcessor.postProcessEnvironment(environment, mock(SpringApplication.class));

		PropertySource<?> propertySource = propertySources.get("defaultProperties");
		Assert.assertNotNull(propertySource);
		Assert.assertNotNull(propertySource.getProperty("spring.cloud.sentinel.filter.enabled"));
	}

	/**
	 * Tests the logic of processing an environment that already contains a property source.
	 * This test case simulates an environment with an existing property source and checks if the post-processor can interact correctly with these property sources.
	 */
	@Test
	public void testPostProcessEnvironmentWithExistingPropertySource() {
		ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
		MutablePropertySources propertySources = new MutablePropertySources();
		when(environment.getPropertySources()).thenReturn(propertySources);

		Map<String, Object> existingProperties = new HashMap<>();
		existingProperties.put("existing.property", "value");
		MapPropertySource existingPropertySource = new MapPropertySource("defaultProperties", existingProperties);
		propertySources.addFirst(existingPropertySource);

		GatewayEnvironmentPostProcessor postProcessor = new GatewayEnvironmentPostProcessor();
		postProcessor.postProcessEnvironment(environment, mock(SpringApplication.class));

		PropertySource<?> propertySource = propertySources.get("defaultProperties");
		Assert.assertNotNull(propertySource);
		Assert.assertEquals("value", propertySource.getProperty("existing.property"));
		Assert.assertEquals("false", propertySource.getProperty("spring.cloud.sentinel.filter.enabled"));
	}

}
