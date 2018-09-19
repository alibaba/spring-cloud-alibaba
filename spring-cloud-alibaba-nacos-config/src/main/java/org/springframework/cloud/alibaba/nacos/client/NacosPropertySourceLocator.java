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

package org.springframework.cloud.alibaba.nacos.client;


import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cloud.alibaba.nacos.NacosConfigProperties;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import static com.alibaba.nacos.api.PropertyKeyConst.*;

/**
 * @author xiaojing
 */
@Order(0)
public class NacosPropertySourceLocator implements PropertySourceLocator {

	private static final Logger logger = LoggerFactory
			.getLogger(NacosPropertySourceLocator.class);
	private static final String NACOS_PROPERTY_SOURCE_NAME = "NACOS";
	private static final String SEP1 = "-";
	private static final String DOT = ".";

	@Autowired
	private ConfigurableListableBeanFactory beanFactory;

	@Autowired
	private NacosConfigProperties nacosConfigProperties;

	private ConfigService configService;

	private NacosPropertySourceBuilder nacosPropertySourceBuilder;

	private Properties getPropertiesFromEnv(Environment env) {

		nacosConfigProperties.overrideFromEnv(env);

		Properties properties = new Properties();
		properties.put(SERVER_ADDR, nacosConfigProperties.getServerAddr());
		properties.put(ENCODE, nacosConfigProperties.getEncode());
		properties.put(NAMESPACE, nacosConfigProperties.getNamespace());
		properties.put(ACCESS_KEY, nacosConfigProperties.getAccessKey());
		properties.put(SECRET_KEY, nacosConfigProperties.getSecretKey());
		properties.put(CONTEXT_PATH, nacosConfigProperties.getContextPath());
		properties.put(CLUSTER_NAME, nacosConfigProperties.getClusterName());
		properties.put(ENDPOINT, nacosConfigProperties.getEndpoint());
		return properties;
	}

	@Override
	public PropertySource<?> locate(Environment env) {

		Properties properties = getPropertiesFromEnv(env);

		try {
			configService =  NacosFactory.createConfigService(properties);
		}
		catch (NacosException e) {
			logger.error("create config service error, nacosConfigProperties:{}, ", properties, e);
			return null;
		}
		
		beanFactory.registerSingleton("configService", configService);

		if (null == configService) {
			logger.warn(
					"no instance of config service found, can't load config from nacos");
			return null;
		}
		long timeout = nacosConfigProperties.getTimeout();
		nacosPropertySourceBuilder = new NacosPropertySourceBuilder(configService, timeout);

		String applicationName = env.getProperty("spring.application.name");
		logger.info("Initialize spring.application.name '" + applicationName + "'.");

		String nacosGroup = nacosConfigProperties.getGroup();
		String dataIdPrefix = nacosConfigProperties.getPrefix();
		if (StringUtils.isEmpty(dataIdPrefix)) {
			dataIdPrefix = applicationName;
		}

		String contentType = nacosConfigProperties.getContentType();

		CompositePropertySource composite = new CompositePropertySource(
				NACOS_PROPERTY_SOURCE_NAME);

		loadApplicationConfiguration(composite, env, nacosGroup, dataIdPrefix, contentType);

		return composite;
	}

	private void loadApplicationConfiguration(
			CompositePropertySource compositePropertySource, Environment environment,
			String nacosGroup, String dataIdPrefix, String contentType) {
		loadNacosDataIfPresent(compositePropertySource, dataIdPrefix + DOT + contentType,
				nacosGroup, contentType);
		for (String profile : environment.getActiveProfiles()) {
			String dataId = dataIdPrefix + SEP1 + profile + DOT + contentType;
			loadNacosDataIfPresent(compositePropertySource, dataId, nacosGroup,
					contentType);
		}
		// todo multi profile active order and priority
	}

	private void loadNacosDataIfPresent(final CompositePropertySource composite,
			final String dataId, final String group,String contentType) {
		NacosPropertySource ps = nacosPropertySourceBuilder.build(dataId, group, contentType);
		if (ps != null) {
			composite.addFirstPropertySource(ps);
		}
	}
}
