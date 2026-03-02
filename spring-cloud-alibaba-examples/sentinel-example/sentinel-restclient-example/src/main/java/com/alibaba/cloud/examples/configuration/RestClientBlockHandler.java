/*
 * Copyright 2013-present the author or authors.
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

import com.alibaba.cloud.sentinel.rest.SentinelClientHttpResponse;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Block handler, fallback and url cleaner for RestClient.
 *
 * @author uuuyuqi
 */
public class RestClientBlockHandler {

	private static final Logger log = LoggerFactory.getLogger(RestClientBlockHandler.class);

	/**
	 * Block handler for flow-control.
	 * When a request is rate-limited, this handler returns a custom response.
	 */
	public static ClientHttpResponse handleBlock(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution, BlockException ex) {
		log.warn("[Customized-RestClientBlockHandler] RestClient blocked by flow control: uri={}, rule={}",
				request.getURI(), ex.getRule());
		return new SentinelClientHttpResponse(
				"[Customized-RestClientBlockHandler] Blocked by flow control: " + ex.getClass().getSimpleName());
	}

	/**
	 * Fallback for circuit-breaking (degrade).
	 * When a downstream service is degraded, this handler returns a fallback response.
	 */
	public static ClientHttpResponse handleFallback(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution, BlockException ex) {
		log.warn("[Customized-RestClientBlockHandler] RestClient degraded (circuit open): uri={}, rule={}",
				request.getURI(), ex.getRule());
		return new SentinelClientHttpResponse("[Customized-RestClientBlockHandler] Service degraded, please try again later");
	}

	/**
	 * URL cleaner to normalize RESTful URLs.
	 * Prevents resource name explosion for parameterized paths.
	 */
	public static String cleanUrl(String url) {
		// Example: normalize /users/123 to /users/{id}
		// In this demo, we return the URL as-is since httpbin.org paths are fixed
		return url;
	}

}
