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

package com.alibaba.cloud.routing.gateway.filter;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.routing.constant.LabelRoutingConstants;
import com.alibaba.cloud.routing.context.LabelRoutingContext;
import com.alibaba.cloud.routing.properties.LabelRoutingProperties;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author yuluo
 * @author <a href="1481556636@qq.com"></a>
 */

public class LabelRoutingGatewayFilter implements GlobalFilter, Ordered {

	// Filter order.
	@Value("${" + LabelRoutingConstants.Gateway.GATEWAY_ROUTE_FILTER_ORDER + ":"
			+ LabelRoutingConstants.Gateway.GATEWAY_ROUTE_FILTER_ORDER_VALUE + "}")
	protected Integer filterOrderNum;

	// Gateway rule priority switch.
	@Value("${" + LabelRoutingConstants.Gateway.GATEWAY_HEADER_PRIORITY + ":true}")
	protected Boolean gatewayRequestHeaderPriority;

	@Resource
	private LabelRoutingProperties properties;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		LabelRoutingContext.getCurrentContext().setExchange(exchange);

		ServerHttpRequest request = exchange.getRequest();
		ServerHttpRequest.Builder requestBuilder = request.mutate();

		applyRequestHeader(request, requestBuilder);

		ServerHttpRequest newRequest = requestBuilder.build();
		ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

		LabelRoutingContext.getCurrentContext().setExchange(newExchange);

		return chain.filter(newExchange);
	}

	private void applyRequestHeader(ServerHttpRequest request,
			ServerHttpRequest.Builder requestBuilder) {

		// Use map to simplify if... else statement
		Map<String, String> propertiesMap = new HashMap<>();
		propertiesMap.put(LabelRoutingConstants.SCA_ROUTING_SERVICE_ZONE,
				properties.getZone());
		LabelRoutingContext.getCurrentContext().setRoutingZone(properties.getZone());
		propertiesMap.put(LabelRoutingConstants.SCA_ROUTING_SERVICE_REGION,
				properties.getRegion());
		LabelRoutingContext.getCurrentContext().setRoutingRegion(properties.getRegion());
		LabelRoutingContext.getCurrentContext().setServerHttpRequest(request);

		propertiesMap.forEach((k, v) -> setRequestHeader(request, requestBuilder, k, v,
				gatewayRequestHeaderPriority));

	}

	@Override
	public int getOrder() {

		return filterOrderNum;
	}

	private void setRequestHeader(ServerHttpRequest request,
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
