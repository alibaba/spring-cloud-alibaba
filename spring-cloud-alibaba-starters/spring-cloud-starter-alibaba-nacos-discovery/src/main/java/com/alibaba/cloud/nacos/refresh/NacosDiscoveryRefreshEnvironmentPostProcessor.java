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

package com.alibaba.cloud.nacos.refresh;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * Keeps Nacos discovery registration state out of generic configuration refreshes.
 *
 * @author hutiefang
 */
public class NacosDiscoveryRefreshEnvironmentPostProcessor
		implements EnvironmentPostProcessor, Ordered {

	private static final String PROPERTY_SOURCE_NAME = "nacosDiscoveryRefreshProperties";

	private static final String NEVER_REFRESHABLE = "spring.cloud.refresh.never-refreshable";

	private static final String DEFAULT_NEVER_REFRESHABLE = "com.zaxxer.hikari.HikariDataSource";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		Set<String> neverRefreshable = new LinkedHashSet<>(
				StringUtils.commaDelimitedListToSet(environment.getProperty(
						NEVER_REFRESHABLE, DEFAULT_NEVER_REFRESHABLE)));
		neverRefreshable.add(NacosDiscoveryProperties.class.getName());

		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put(NEVER_REFRESHABLE,
				StringUtils.collectionToCommaDelimitedString(neverRefreshable));
		environment.getPropertySources().addFirst(
				new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

}
