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

package org.springframework.cloud.alibaba.fescar;

import com.alibaba.fescar.spring.annotation.GlobalTransactionScanner;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author xiaojing
 */

@Configuration
@EnableConfigurationProperties(FescarProperties.class)
public class GlobalTransactionAutoConfiguration {

	private final ApplicationContext applicationContext;

	private final FescarProperties fescarProperties;

	public GlobalTransactionAutoConfiguration(ApplicationContext applicationContext,
			FescarProperties fescarProperties) {
		this.applicationContext = applicationContext;
		this.fescarProperties = fescarProperties;
	}

	@Bean
	public GlobalTransactionScanner globalTransactionScanner() {

		String applicationName = applicationContext.getEnvironment()
				.getProperty("spring.application.name");

		String txServiceGroup = fescarProperties.getTxServiceGroup();

		if (StringUtils.isEmpty(txServiceGroup)) {
			txServiceGroup = applicationName + "-fescar-service-group";
			fescarProperties.setTxServiceGroup(txServiceGroup);
		}

		return new GlobalTransactionScanner(applicationName, txServiceGroup);
	}
}
