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

package com.alibaba.cloud.nacos;

import com.alibaba.cloud.nacos.diagnostics.analyzer.NacosConnectionFailureException;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Manager class for Nacos configuration service.
 * Provides singleton access to Nacos ConfigService instance and manages configuration properties.
 *
 * @author zkzlx
 */
public class NacosConfigManager {

	private static final Logger log = LoggerFactory.getLogger(NacosConfigManager.class);

	// Singleton Nacos ConfigService instance
	private static ConfigService service;

	// Singleton instance of NacosConfigManager
	private static NacosConfigManager INSTANCE;

	// Configuration properties for Nacos config
	private NacosConfigProperties nacosConfigProperties;

	/**
	 * Gets the singleton instance of NacosConfigManager.
	 * Note: This method returns null if the instance has not been initialized via getInstance(NacosConfigProperties).
	 *
	 * @return singleton NacosConfigManager instance, or null if not initialized
	 */
	public NacosConfigManager(NacosConfigProperties nacosConfigProperties) {
		this.nacosConfigProperties = nacosConfigProperties;
	}

	/**
	 * Gets the singleton instance of NacosConfigManager.
	 * Note: This method returns null if the instance has not been initialized via getInstance(NacosConfigProperties).
	 *
	 * @return singleton NacosConfigManager instance, or null if not initialized
	 */
	public static NacosConfigManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Gets or initializes the singleton instance of NacosConfigManager with given properties.
	 * Thread-safe initialization of the singleton instance.
	 *
	 * @param properties Nacos configuration properties
	 * @return initialized singleton NacosConfigManager instance
	 */
	public static NacosConfigManager getInstance(NacosConfigProperties properties) {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		synchronized (NacosConfigManager.class) {
			if (INSTANCE == null) {
				INSTANCE = new NacosConfigManager(properties);
				INSTANCE.createConfigService(properties);
			}
		}
		return INSTANCE;
	}

	/**
	 * Creates Nacos ConfigService instance with given properties (compatible with legacy design).
	 * This method will be optimized in future releases.
	 *
	 * @param nacosConfigProperties configuration properties for Nacos ConfigService
	 * @return created ConfigService instance
	 * @throws NacosConnectionFailureException if failed to connect to Nacos server
	 */
	private ConfigService createConfigService(
			NacosConfigProperties nacosConfigProperties) {
		try {
			if (Objects.isNull(service)) {
				service = NacosFactory.createConfigService(
						nacosConfigProperties.assembleConfigServiceProperties());
			}
		}
		catch (NacosException e) {
			log.error(e.getMessage());
			throw new NacosConnectionFailureException(
					nacosConfigProperties.getServerAddr(), e.getMessage(), e);
		}
		return service;
	}

	/**
	 * Gets the singleton Nacos ConfigService instance.
	 * Initializes the ConfigService if it is not already created.
	 *
	 * @return singleton ConfigService instance
	 * @throws NacosConnectionFailureException if failed to create ConfigService
	 */
	public ConfigService getConfigService() {
		if (Objects.isNull(service)) {
			createConfigService(this.nacosConfigProperties);
		}
		return service;
	}

	/**
	 * Gets the Nacos configuration properties used by this manager.
	 *
	 * @return NacosConfigProperties instance
	 */
	public NacosConfigProperties getNacosConfigProperties() {
		return nacosConfigProperties;
	}

}
