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

package com.alibaba.cloud.opensergo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.alibaba.cloud.opensergo.condition.OnEnvCondition;
import com.alibaba.cloud.opensergo.reportor.ServiceContractReporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author luyanbo
 */
@Configuration(proxyBeanMethods = false)
@Conditional(OnEnvCondition.class)
public class OpenSergoAutoConfiguration {
	private static final Logger log = LoggerFactory
			.getLogger(OpenSergoAutoConfiguration.class);

	@Bean
	public ServiceContractReporter serviceContractReporter(ApplicationContext context)
			throws IOException {
		String bootstrap = context.getEnvironment()
				.getProperty(OpenSergoConstants.OPENSERGO_BOOTSTRAP);
		String bootstrapConfig = context.getEnvironment()
				.getProperty(OpenSergoConstants.OPENSERGO_BOOTSTRAP_CONFIG);

		if (!StringUtils.hasLength(bootstrapConfig) && StringUtils.hasLength(bootstrap)) {
			byte[] encoded = Files.readAllBytes(Paths.get(bootstrap));
			bootstrapConfig = new String(encoded, StandardCharsets.UTF_8);
		}

		ObjectMapper objectMapper = new ObjectMapper();
		OpenSergoProperties properties = objectMapper.readValue(bootstrapConfig,
				OpenSergoProperties.class);

		ServiceContractReporter reporter = new ServiceContractReporter(
				properties.getEndpoint());
		reporter.setApplicationContext(context);
		return reporter;
	}
}
