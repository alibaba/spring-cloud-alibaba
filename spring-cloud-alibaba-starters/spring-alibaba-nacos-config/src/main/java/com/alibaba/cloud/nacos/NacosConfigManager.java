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

import java.util.Objects;

import com.alibaba.cloud.nacos.configdata.NacosConfigDataLoadProperties;
import com.alibaba.cloud.nacos.configdata.NacosConfigDataLocationResolver;
import com.alibaba.cloud.nacos.diagnostics.analyzer.NacosConnectionFailureException;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

/**
 * @author zkzlx
 */
public class NacosConfigManager implements InitializingBean, EnvironmentAware {

	private static final Logger log = LoggerFactory.getLogger(NacosConfigManager.class);

	private static ConfigService service;

	private static volatile NacosConfigManager INSTANCE;

	private NacosConfigProperties nacosConfigProperties;

	/**
	 * The Spring environment used to bind properties.
	 */
	private Environment environment;

	/*
	 * Handler for binding configuration properties.
	 */
	private static volatile BindHandler bindHandler;

	public NacosConfigManager(NacosConfigProperties nacosConfigProperties) {
		this.nacosConfigProperties = nacosConfigProperties;
	}

	public static NacosConfigManager getInstance() {
		return INSTANCE;
	}

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
	 * Compatible with old design,It will be perfected in the future.
	 */
	private ConfigService createConfigService(
			NacosConfigProperties nacosConfigProperties) {
		try {
			if (Objects.isNull(service)) {
				synchronized (NacosConfigManager.class) {
					if (Objects.isNull(service)) {
						log.info("Creating new Nacos ConfigService");
						service = NacosFactory.createConfigService(
								nacosConfigProperties.assembleConfigServiceProperties());
						log.info("Successfully created Nacos ConfigService");
					}
				}
			}
		}
		catch (NacosException e) {
			log.error("Failed to create Nacos ConfigService: " + e.getMessage());
			throw new NacosConnectionFailureException(
					nacosConfigProperties.getServerAddr(), e.getMessage(), e);
		}
		return service;
	}

	public ConfigService getConfigService() {
		if (Objects.isNull(service)) {
			createConfigService(this.nacosConfigProperties);
		}
		return service;
	}

	public NacosConfigProperties getNacosConfigProperties() {
		return nacosConfigProperties;
	}

	/**
	 * Sets the Spring environment, used for property binding.
	 *
	 * @param environment the Spring environment
	 */
	@Override
	public void setEnvironment(@NonNull Environment environment) {
		this.environment = environment;
	}

	/**
	 * Called after properties are set, re-binds configuration from the environment
	 * and updates the internal configuration properties and ConfigService accordingly.
	 */
	@Override
	public void afterPropertiesSet() {
		// Rebinds environment properties to NacosConfigProperties
		Binder binder = Binder.get(environment);
		String nacosPrefix = NacosPropertiesPrefixer.getPrefix(binder);
		String nacosConfigPrefix = nacosPrefix + ".config";
		NacosConfigProperties newProperties = binder
				.bind(nacosPrefix, Bindable.of(NacosConfigDataLoadProperties.class), bindHandler)
				.map(properties -> binder
						.bind(nacosConfigPrefix, Bindable.ofInstance(properties), bindHandler)
						.orElse(properties))
				.orElseGet(() -> binder
						.bind(nacosConfigPrefix, Bindable.of(NacosConfigDataLoadProperties.class), bindHandler)
						.orElse(null));
		if (Objects.nonNull(newProperties)) {
			updateInstanceProperties(newProperties);
		}
	}

	/**
	 * Safely update the singleton instance properties in a thread-safe manner.
	 *
	 * @param newProperties the new Nacos config properties
	 */
	private void updateInstanceProperties(NacosConfigProperties newProperties) {
		// Update current instance's properties
		this.nacosConfigProperties = newProperties;
		// Safely update singleton instance
		if (INSTANCE != null) {
			synchronized (NacosConfigManager.class) {
				if (INSTANCE != null && INSTANCE == this) {
					log.debug("Updating INSTANCE properties");
					INSTANCE.nacosConfigProperties = newProperties;
					// Recreate ConfigService if needed
					recreateConfigService(newProperties);
				}
				else {
					log.debug("Skipping update of INSTANCE properties as it does not match current instance");
				}
			}
		}
	}

	/**
	 * Recreates the ConfigService using new properties after shutting down the previous one.
	 *
	 * @param properties the new Nacos config properties
	 */
	private void recreateConfigService(NacosConfigProperties properties) {
		synchronized (NacosConfigManager.class) {
			log.info("Recreating Nacos ConfigService");
			if (service != null) {
				try {
					service.shutDown();
					log.info("Shutdown old Nacos ConfigService");
				}
				catch (NacosException e) {
					log.error("Failed to shutdown old Nacos ConfigService. Current service may be in inconsistent state. " +
							"Server address: " + properties.getServerAddr() + ", Group: " + properties.getGroup() +
							", Namespace: " + properties.getNamespace(), e);
				}
				service = null;
			}
			createConfigService(properties);
			log.info("Successfully recreated Nacos ConfigService");
		}
	}

	/**
	 * Sets the bind handler for configuration properties binding.
	 * <p>This method is typically called during early Spring startup phase,
	 * such as in {@link NacosConfigDataLocationResolver} class.</p>
	 *
	 * @param handler the bind handler to set
	 */
	public static void setBindHandler(BindHandler handler) {
		if (bindHandler == null) {
			synchronized (NacosConfigManager.class) {
				if (bindHandler == null) {
					bindHandler = handler;
				}
			}
		}
	}

	/**
	 * Get BindHandler.
	 *
	 */
	public static BindHandler getBindHandler() {
		return bindHandler;
	}

}
