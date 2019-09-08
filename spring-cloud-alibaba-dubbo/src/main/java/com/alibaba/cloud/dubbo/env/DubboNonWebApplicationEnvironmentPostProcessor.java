/*
 * Copyright (C) 2018 the original author or authors.
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
package com.alibaba.cloud.dubbo.env;

import static org.apache.dubbo.common.constants.CommonConstants.DEFAULT_PROTOCOL;
import static org.apache.dubbo.config.spring.util.PropertySourcesUtils.getSubProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Dubbo {@link WebApplicationType#NONE Non-Web Application}
 * {@link EnvironmentPostProcessor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboNonWebApplicationEnvironmentPostProcessor
		implements EnvironmentPostProcessor, Ordered {

	private static final String DOT = ".";

	/**
	 * The name of default {@link PropertySource} defined in
	 * SpringApplication#configurePropertySources method.
	 */
	private static final String PROPERTY_SOURCE_NAME = "defaultProperties";

	private static final String SERVER_PORT_PROPERTY_NAME = "server.port";

	private static final String PORT_PROPERTY_NAME = "port";

	private static final String PROTOCOL_PROPERTY_NAME_PREFIX = "dubbo.protocol";

	private static final String PROTOCOL_NAME_PROPERTY_NAME_SUFFIX = DOT + "name";

	private static final String PROTOCOL_PORT_PROPERTY_NAME_SUFFIX = DOT
			+ PORT_PROPERTY_NAME;

	private static final String PROTOCOL_PORT_PROPERTY_NAME = PROTOCOL_PROPERTY_NAME_PREFIX
			+ PROTOCOL_PORT_PROPERTY_NAME_SUFFIX;

	private static final String PROTOCOL_NAME_PROPERTY_NAME = PROTOCOL_PROPERTY_NAME_PREFIX
			+ PROTOCOL_NAME_PROPERTY_NAME_SUFFIX;

	private static final String PROTOCOLS_PROPERTY_NAME_PREFIX = "dubbo.protocols";

	private static final String REST_PROTOCOL = "rest";

	private final Logger logger = LoggerFactory
			.getLogger(DubboNonWebApplicationEnvironmentPostProcessor.class);

	private static boolean isRestProtocol(String protocol) {
		return REST_PROTOCOL.equalsIgnoreCase(protocol);
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		WebApplicationType webApplicationType = application.getWebApplicationType();

		if (!WebApplicationType.NONE.equals(webApplicationType)) { // Just works in
																	// Non-Web Application
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Current application is a Web Application, the process will be ignored.");
			}
			return;
		}

		MutablePropertySources propertySources = environment.getPropertySources();
		Map<String, Object> defaultProperties = createDefaultProperties(environment);
		if (!CollectionUtils.isEmpty(defaultProperties)) {
			addOrReplace(propertySources, defaultProperties);
		}
	}

	private Map<String, Object> createDefaultProperties(
			ConfigurableEnvironment environment) {
		Map<String, Object> defaultProperties = new HashMap<String, Object>();
		resetServerPort(environment, defaultProperties);
		return defaultProperties;
	}

	/**
	 * Reset server port property if it's absent, whose value is configured by
	 * "dubbbo.protocol.port" or "dubbo.protcols.rest.port"
	 *
	 * @param environment
	 * @param defaultProperties
	 */
	private void resetServerPort(ConfigurableEnvironment environment,
			Map<String, Object> defaultProperties) {

		String serverPort = environment.getProperty(SERVER_PORT_PROPERTY_NAME,
				environment.getProperty(PORT_PROPERTY_NAME));

		if (serverPort != null) {
			return;
		}

		serverPort = getRestPortFromProtocolProperty(environment);

		if (serverPort == null) {
			serverPort = getRestPortFromProtocolsProperties(environment);
		}

		setServerPort(environment, serverPort, defaultProperties);
	}

	private String getRestPortFromProtocolProperty(ConfigurableEnvironment environment) {

		String protocol = environment.getProperty(PROTOCOL_NAME_PROPERTY_NAME,
				DEFAULT_PROTOCOL);

		return isRestProtocol(protocol)
				? environment.getProperty(PROTOCOL_PORT_PROPERTY_NAME)
				: null;
	}

	private String getRestPortFromProtocolsProperties(
			ConfigurableEnvironment environment) {

		String restPort = null;

		Map<String, Object> subProperties = getSubProperties(environment,
				PROTOCOLS_PROPERTY_NAME_PREFIX);

		Properties properties = new Properties();

		properties.putAll(subProperties);

		for (String propertyName : properties.stringPropertyNames()) {
			if (propertyName.endsWith(PROTOCOL_NAME_PROPERTY_NAME_SUFFIX)) { // protocol
																				// name
																				// property
				String protocol = properties.getProperty(propertyName);
				if (isRestProtocol(protocol)) {
					String beanName = resolveBeanName(propertyName);
					if (StringUtils.hasText(beanName)) {
						restPort = properties.getProperty(
								beanName + PROTOCOL_PORT_PROPERTY_NAME_SUFFIX);
						break;
					}
				}
			}
		}

		return restPort;
	}

	private String resolveBeanName(String propertyName) {
		int index = propertyName.indexOf(DOT);
		return index > -1 ? propertyName.substring(0, index) : null;
	}

	private void setServerPort(ConfigurableEnvironment environment, String serverPort,
			Map<String, Object> defaultProperties) {
		if (serverPort == null) {
			return;
		}

		defaultProperties.put(SERVER_PORT_PROPERTY_NAME, serverPort);

	}

	/**
	 * Copy from BusEnvironmentPostProcessor#addOrReplace(MutablePropertySources, Map)
	 *
	 * @param propertySources {@link MutablePropertySources}
	 * @param map Default Dubbo Properties
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
	public int getOrder() { // Keep LOWEST_PRECEDENCE
		return LOWEST_PRECEDENCE;
	}
}
