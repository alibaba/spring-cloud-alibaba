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

package com.alibaba.cloud.nacos;

import java.util.Objects;

import com.alibaba.cloud.nacos.diagnostics.analyzer.NacosConnectionFailureException;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.factory.CacheableEventPublishingNacosServiceFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
public class NacosConfigManager implements ApplicationContextAware {

	private static ConfigService service = null;

	private static final CacheableEventPublishingNacosServiceFactory SERVICE_FACTORY = CacheableEventPublishingNacosServiceFactory
			.getSingleton();

	@Autowired
	private NacosConfigProperties properties;

	public ConfigService getConfigService() {
		if (Objects.isNull(service)) {
			try {
				// Using cache object creation factory
				service = SERVICE_FACTORY
						.createConfigService(properties.getConfigServiceProperties());
				properties.initConfigService(service);
			}
			catch (NacosException e) {
				throw new NacosConnectionFailureException(properties.getServerAddr(),
						e.getMessage(), e);
			}
		}
		return service;
	}

	// Whenever the Context refresh NacosServiceFactory need change perception

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		SERVICE_FACTORY.setApplicationContext(applicationContext);
	}

}
