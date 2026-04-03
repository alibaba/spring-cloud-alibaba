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

import java.util.Objects;

import com.alibaba.cloud.nacos.diagnostics.analyzer.NacosConnectionFailureException;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zkzlx
 */
public class NacosConfigManager {

	private static final Logger log = LoggerFactory.getLogger(NacosConfigManager.class);

	private @Nullable static ConfigService service;

	private @Nullable static NacosConfigManager INSTANCE;

	private final NacosConfigProperties nacosConfigProperties;

	public NacosConfigManager(NacosConfigProperties nacosConfigProperties) {
		this.nacosConfigProperties = nacosConfigProperties;
	}

	public static @Nullable NacosConfigManager getInstance() {
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
				service = NacosFactory.createConfigService(
						nacosConfigProperties.assembleConfigServiceProperties());
			}
		}
		catch (NacosException e) {
			String serverAddr = nacosConfigProperties.getServerAddr();
			String message = e.getMessage();
			log.error(message != null ? message : "NacosException");
			throw new NacosConnectionFailureException(
					serverAddr != null ? serverAddr : "", message != null ? message : "", e);
		}
		return service;
	}

	public ConfigService getConfigService() {
		if (Objects.isNull(service)) {
			createConfigService(this.nacosConfigProperties);
		}
		if (service == null) {
			throw new IllegalStateException("ConfigService is not initialized");
		}
		return service;
	}

	public NacosConfigProperties getNacosConfigProperties() {
		return nacosConfigProperties;
	}

}
