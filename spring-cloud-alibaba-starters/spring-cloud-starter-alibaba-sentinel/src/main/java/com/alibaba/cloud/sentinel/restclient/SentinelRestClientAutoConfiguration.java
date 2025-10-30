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
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * Auto-configuration for Sentinel integration with Spring's RestClient.
 * Controlled by the global switch:
 *   spring.cloud.sentinel.enabled (default: true)
 * When disabled, none of the RestClient-related Sentinel beans are registered.
 */
@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@ConditionalOnProperty(
		prefix = "spring.cloud.sentinel",
		name = "enabled",
		havingValue = "true",
		matchIfMissing = true
)
@EnableConfigurationProperties(SentinelRestClientProperties.class)
public class SentinelRestClientAutoConfiguration {

	/**
	 * Sentinel interceptor for RestClient.
	 * Do NOT check any 'enabled' flag here; gating is done by ConditionalOnProperty.
	 */
	@Bean
	@ConditionalOnMissingBean
	public SentinelRestClientInterceptor sentinelRestClientInterceptor() {
		return new SentinelRestClientInterceptor();
	}

	/**
	 * Register interceptor to Spring-managed RestClient.Builder.
	 * Insert at index 0 to make sure the interceptor takes precedence.
	 */
	@Bean
	public BeanPostProcessor sentinelRestClientBeanPostProcessor(
			SentinelRestClientInterceptor interceptor) {
		return new SentinelRestClientBeanPostProcessor(interceptor);
	}
}
