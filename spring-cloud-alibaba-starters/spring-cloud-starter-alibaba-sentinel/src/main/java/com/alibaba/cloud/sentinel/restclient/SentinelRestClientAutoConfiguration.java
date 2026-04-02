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

package com.alibaba.cloud.sentinel.restclient;

import com.alibaba.cloud.sentinel.annotation.SentinelRestClient;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * Auto-configuration for Sentinel integration with Spring's RestClient.
 *
 * <p>
 * Controlled by the global switch:
 * {@code spring.cloud.sentinel.enabled} (default: true).
 * When disabled, none of the RestClient-related Sentinel beans are registered.
 *
 * <p>
 * To enable Sentinel for a RestClient, use
 * {@link SentinelRestClient}
 * annotation on the {@code @Bean} method that returns
 * {@link RestClient.Builder}:
 *
 * <pre>
 * &#64;Bean
 * &#64;SentinelRestClient(blockHandler = "handleBlock",
 *     blockHandlerClass = MyBlockHandler.class)
 * public RestClient.Builder restClientBuilder() {
 *     return RestClient.builder();
 * }
 * </pre>
 *
 * @author QHT, uuuyuqi
 * @see SentinelRestClient
 * @see SentinelRestClientInterceptor
 */
@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@ConditionalOnProperty(prefix = "spring.cloud.sentinel", name = "enabled",
		havingValue = "true", matchIfMissing = true)
public class SentinelRestClientAutoConfiguration {

	/**
	 * BeanPostProcessor that handles
	 * {@link SentinelRestClient}
	 * annotation and adds interceptor to RestClient.Builder.
	 * @param applicationContext the application context
	 * @return the bean post processor
	 */
	@Bean
	public SentinelRestClientBeanPostProcessor sentinelRestClientBeanPostProcessor(
			ApplicationContext applicationContext) {
		return new SentinelRestClientBeanPostProcessor(applicationContext);
	}

}
