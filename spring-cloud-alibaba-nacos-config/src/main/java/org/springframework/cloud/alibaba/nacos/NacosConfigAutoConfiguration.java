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

package org.springframework.cloud.alibaba.nacos;

import com.alibaba.nacos.api.config.ConfigService;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.nacos.refresh.NacosContextRefresher;
import org.springframework.cloud.alibaba.nacos.refresh.NacosRefreshHistory;
import org.springframework.cloud.alibaba.nacos.refresh.NacosRefreshProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author juven.xuxb
 */
@Configuration
public class NacosConfigAutoConfiguration implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Autowired
	private NacosConfigProperties nacosConfigProperties;

	@Autowired
	private NacosRefreshProperties nacosRefreshProperties;

	@Autowired
	private ConfigService configService;

	@Bean
	public NacosConfigProperties nacosConfigProperties() {
		return new NacosConfigProperties();
	}

	@Bean
	public NacosPropertySourceRepository nacosPropertySourceRepository() {
		return new NacosPropertySourceRepository(applicationContext);
	}

	@Bean
	public NacosRefreshProperties nacosRefreshProperties() {
		return new NacosRefreshProperties();
	}

	@Bean
	public NacosRefreshHistory nacosRefreshHistory() {
		return new NacosRefreshHistory();
	}

	@Bean
	public NacosContextRefresher nacosContextRefresher(ContextRefresher contextRefresher,
			NacosRefreshHistory refreshHistory,
			NacosPropertySourceRepository propertySourceRepository,
													   ConfigService configService) {
		return new NacosContextRefresher(contextRefresher, nacosConfigProperties,
				nacosRefreshProperties, refreshHistory, propertySourceRepository,configService);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
