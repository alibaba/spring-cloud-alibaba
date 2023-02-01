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

package com.alibaba.cloud.examples.example;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Configuration listener example.
 *
 * @author lixiaoshuang
 */
@Component
public class ConfigListenerExample {

	Logger logger = LoggerFactory.getLogger(ConfigListenerExample.class);

	/**
	 * Nacos dataId.
	 */
	public static final String DATA_ID = "nacos-config-example.properties";

	/**
	 * Nacos group.
	 */
	public static final String GROUP = "DEFAULT_GROUP";

	@Autowired
	private NacosConfigManager nacosConfigManager;

	@PostConstruct
	public void init() throws NacosException {
		ConfigService configService = nacosConfigManager.getConfigService();

		configService.addListener(DATA_ID, GROUP, new Listener() {
			@Override
			public Executor getExecutor() {
				return Executors.newSingleThreadExecutor();
			}

			@Override
			public void receiveConfigInfo(String configInfo) {
				logger.info("[dataId]:[" + DATA_ID + "],Configuration changed to:"
						+ configInfo);
			}
		});
	}

}
