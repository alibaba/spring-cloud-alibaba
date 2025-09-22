/*
 * Copyright 2013-2025 the original author or authors.
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

package com.alibaba.cloud.sentinel.restclient;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;


@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@EnableConfigurationProperties(SentinelRestClientProperties.class)
public class SentinelRestClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SentinelRestClientInterceptor sentinelRestClientInterceptor(
			SentinelRestClientProperties properties) {
		return new SentinelRestClientInterceptor(properties);
	}

	@Bean
	@ConditionalOnMissingBean
	public RestClient.Builder restClientBuilder(SentinelRestClientInterceptor interceptor) {
		return RestClient.builder()
				.requestInterceptors(list -> list.add(interceptor));
	}

	@Bean
	public SentinelRestClientBeanPostProcessor sentinelRestClientBeanPostProcessor(
			SentinelRestClientInterceptor interceptor) {
		return new SentinelRestClientBeanPostProcessor(interceptor);
	}
}
