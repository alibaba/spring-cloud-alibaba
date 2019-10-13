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

<<<<<<< HEAD
=======
import java.util.Objects;

import com.alibaba.nacos.api.NacosFactory;
>>>>>>> 1773b49872437dd18b80b7bb2ede42b2de7b7b0b
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.factory.CacheableEventPublishingNacosServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
<<<<<<< HEAD
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
=======
>>>>>>> 1773b49872437dd18b80b7bb2ede42b2de7b7b0b

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
public class NacosConfigManager implements ApplicationContextAware {

	private static final Logger log = LoggerFactory.getLogger(NacosConfigManager.class);

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
				log.error("create config service error!properties={},e=,", properties, e);
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
