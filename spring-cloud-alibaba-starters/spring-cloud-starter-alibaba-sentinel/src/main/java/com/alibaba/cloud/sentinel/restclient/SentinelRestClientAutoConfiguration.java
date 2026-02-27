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

import java.util.function.BiFunction;
import java.util.function.Function;

import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
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
 * Users can customize behavior by registering the following beans:
 * <ul>
 * <li>A {@code Function<String, String>} bean named
 * {@value #URL_CLEANER_BEAN_NAME}
 * for URL cleaning/normalization</li>
 * <li>A {@code BiFunction<HttpRequest, BlockException, ClientHttpResponse>}
 * bean named
 * {@value #BLOCK_HANDLER_BEAN_NAME} for handling flow-control blocks</li>
 * <li>A {@code BiFunction<HttpRequest, BlockException, ClientHttpResponse>}
 * bean named
 * {@value #FALLBACK_BEAN_NAME} for handling circuit-breaking (degrade)
 * blocks</li>
 * </ul>
 *
 * @author QHT, uuuyuqi
 * @see SentinelRestClientInterceptor
 */
@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@ConditionalOnProperty(prefix = "spring.cloud.sentinel", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SentinelRestClientAutoConfiguration {

	/**
	 * Bean name for the URL cleaner function.
	 */
	public static final String URL_CLEANER_BEAN_NAME = "sentinelRestClientUrlCleaner";

	/**
	 * Bean name for the block handler function.
	 */
	public static final String BLOCK_HANDLER_BEAN_NAME = "sentinelRestClientBlockHandler";

	/**
	 * Bean name for the fallback function.
	 */
	public static final String FALLBACK_BEAN_NAME = "sentinelRestClientFallback";

	/**
	 * Sentinel interceptor for RestClient.
	 */
	@Bean
	@ConditionalOnMissingBean
	public SentinelRestClientInterceptor sentinelRestClientInterceptor(
			@Qualifier(URL_CLEANER_BEAN_NAME) ObjectProvider<Function<String, String>> urlCleanerProvider,
			@Qualifier(BLOCK_HANDLER_BEAN_NAME) ObjectProvider<BiFunction<HttpRequest, BlockException, ClientHttpResponse>> blockHandlerProvider,
			@Qualifier(FALLBACK_BEAN_NAME) ObjectProvider<BiFunction<HttpRequest, BlockException, ClientHttpResponse>> fallbackProvider) {
		return new SentinelRestClientInterceptor(
				urlCleanerProvider.getIfAvailable(),
				blockHandlerProvider.getIfAvailable(),
				fallbackProvider.getIfAvailable());
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
