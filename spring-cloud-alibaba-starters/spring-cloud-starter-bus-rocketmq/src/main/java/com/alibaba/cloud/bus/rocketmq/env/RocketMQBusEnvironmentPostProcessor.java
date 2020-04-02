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

package com.alibaba.cloud.bus.rocketmq.env;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.cloud.bus.BusEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import static org.springframework.cloud.bus.SpringCloudBusClient.INPUT;

/**
 * The lowest precedence {@link EnvironmentPostProcessor} configures default RocketMQ Bus
 * Properties that will be appended into {@link SpringApplication#defaultProperties}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.2.1
 * @see BusEnvironmentPostProcessor
 */
public class RocketMQBusEnvironmentPostProcessor
		implements EnvironmentPostProcessor, Ordered {

	/**
	 * The name of {@link PropertySource} of {@link SpringApplication#defaultProperties}.
	 */
	private static final String PROPERTY_SOURCE_NAME = "defaultProperties";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {

		addDefaultPropertySource(environment);

	}

	private void addDefaultPropertySource(ConfigurableEnvironment environment) {

		Map<String, Object> map = new HashMap<String, Object>();

		configureDefaultProperties(map);

		addOrReplace(environment.getPropertySources(), map);
	}

	private void configureDefaultProperties(Map<String, Object> source) {
		// Required Properties
		String groupBindingPropertyName = createBindingPropertyName(INPUT, "group");
		String broadcastingPropertyName = createRocketMQPropertyName(INPUT,
				"broadcasting");
		source.put(groupBindingPropertyName, "rocketmq-bus-group");
		source.put(broadcastingPropertyName, "true");
	}

	private String createRocketMQPropertyName(String channel, String propertyName) {
		return "spring.cloud.stream.rocketmq.bindings." + INPUT + ".consumer."
				+ propertyName;
	}

	private String createBindingPropertyName(String channel, String propertyName) {
		return "spring.cloud.stream.bindings." + channel + "." + propertyName;
	}

	/**
	 * Copy from.
	 * {@link BusEnvironmentPostProcessor#addOrReplace(MutablePropertySources, Map)}
	 * @param propertySources {@link MutablePropertySources}
	 * @param map Default RocketMQ Bus Properties
	 */
	private void addOrReplace(MutablePropertySources propertySources,
			Map<String, Object> map) {
		MapPropertySource target = null;
		if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
			PropertySource<?> source = propertySources.get(PROPERTY_SOURCE_NAME);
			if (source instanceof MapPropertySource) {
				target = (MapPropertySource) source;
				for (String key : map.keySet()) {
					if (!target.containsProperty(key)) {
						target.getSource().put(key, map.get(key));
					}
				}
			}
		}
		if (target == null) {
			target = new MapPropertySource(PROPERTY_SOURCE_NAME, map);
		}
		if (!propertySources.contains(PROPERTY_SOURCE_NAME)) {
			propertySources.addLast(target);
		}
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

}
