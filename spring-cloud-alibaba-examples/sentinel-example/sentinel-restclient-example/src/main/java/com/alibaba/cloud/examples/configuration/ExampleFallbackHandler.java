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

package com.alibaba.cloud.examples.configuration;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.alibaba.cloud.sentinel.restclient.SentinelRestClientAutoConfiguration;
import com.alibaba.cloud.sentinel.restclient.SentinelRestClientHttpResponse;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Example configuration demonstrating how to customize Sentinel behavior for RestClient.
 *
 * <p>Register beans with specific names to customize:
 * <ul>
 *   <li>{@code sentinelRestClientBlockHandler}: handles flow-control blocks</li>
 *   <li>{@code sentinelRestClientFallback}: handles circuit-breaking (degrade) blocks</li>
 *   <li>{@code sentinelRestClientUrlCleaner}: normalizes resource names</li>
 * </ul>
 *
 * @author uuuyuqi
 */
@Configuration
public class ExampleFallbackHandler {

	private static final Logger log = LoggerFactory.getLogger(ExampleFallbackHandler.class);

	/**
	 * Custom block handler for flow-control.
	 * When a request is rate-limited, this handler returns a custom response.
	 */
	@Bean(SentinelRestClientAutoConfiguration.BLOCK_HANDLER_BEAN_NAME)
	public BiFunction<HttpRequest, BlockException, ClientHttpResponse> sentinelRestClientBlockHandler() {
		return (request, ex) -> {
			log.warn("RestClient blocked by flow control: uri={}, rule={}",
					request.getURI(), ex.getRule());
			return new SentinelRestClientHttpResponse(
					"Blocked by flow control: " + ex.getClass().getSimpleName());
		};
	}

	/**
	 * Custom fallback for circuit-breaking (degrade).
	 * When a downstream service is degraded, this handler returns a fallback response.
	 */
	@Bean(SentinelRestClientAutoConfiguration.FALLBACK_BEAN_NAME)
	public BiFunction<HttpRequest, BlockException, ClientHttpResponse> sentinelRestClientFallback() {
		return (request, ex) -> {
			log.warn("RestClient degraded (circuit open): uri={}, rule={}",
					request.getURI(), ex.getRule());
			return new SentinelRestClientHttpResponse(
					"Service degraded, please try again later");
		};
	}

	/**
	 * URL cleaner to normalize RESTful URLs.
	 * Prevents resource name explosion for parameterized paths.
	 */
	@Bean(SentinelRestClientAutoConfiguration.URL_CLEANER_BEAN_NAME)
	public Function<String, String> sentinelRestClientUrlCleaner() {
		return url -> {
			// Example: normalize /users/123 to /users/{id}
			// In this demo, we return the URL as-is since httpbin.org paths are fixed
			return url;
		};
	}

}
