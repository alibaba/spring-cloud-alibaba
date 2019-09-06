/*
 * Copyright (C) 2019 the original author or authors.
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

package com.alibaba.cloud.seata;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import io.seata.spring.annotation.GlobalTransactionScanner;

/**
 * @author xiaojing
 */

@Configuration
@EnableConfigurationProperties(SeataProperties.class)
public class GlobalTransactionAutoConfiguration {

	private final ApplicationContext applicationContext;

	private final SeataProperties seataProperties;

	public GlobalTransactionAutoConfiguration(ApplicationContext applicationContext,
			SeataProperties seataProperties) {
		this.applicationContext = applicationContext;
		this.seataProperties = seataProperties;
	}

	@Bean
	public GlobalTransactionScanner globalTransactionScanner() {

		String applicationName = applicationContext.getEnvironment()
				.getProperty("spring.application.name");

		String txServiceGroup = seataProperties.getTxServiceGroup();

		if (StringUtils.isEmpty(txServiceGroup)) {
			txServiceGroup = applicationName + "-seata-service-group";
			seataProperties.setTxServiceGroup(txServiceGroup);
		}

		return new GlobalTransactionScanner(applicationName, txServiceGroup);
	}
}
