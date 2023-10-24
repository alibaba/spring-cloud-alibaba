/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.gateway.util;

import com.alibaba.cloud.commons.lang.StringUtils;

import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public final class LabelRoutingGatewayFilterResolver {

	private LabelRoutingGatewayFilterResolver() {
	}

	public static void setRequestHeader(ServerHttpRequest request,
			ServerHttpRequest.Builder requestBuilder, String requestHeaderName,
			String requestHeaderValue, Boolean gatewayRequestHeaderPriority) {

		if (StringUtils.isEmpty(requestHeaderValue)) {
			return;
		}

		if (gatewayRequestHeaderPriority) {

			// Under the gateway priority condition, clear all external headers.
			requestBuilder.headers(headers -> headers.remove(requestHeaderName));

			// Add the header set by the gateway.
			requestBuilder.headers(
					headers -> headers.add(requestHeaderName, requestHeaderValue));
		}
		else {
			/**
			 * If the gateway is not prioritized, determine whether the external request
			 * contains headers. If it does, the built-in header is not added.
			 */
			if (!request.getHeaders().containsKey(requestHeaderName)) {
				requestBuilder.headers(
						headers -> headers.add(requestHeaderName, requestHeaderValue));
			}
		}

	}

}
