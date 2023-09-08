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

package com.alibaba.cloud.routing.gateway.filter.defaults;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.cloud.routing.constant.RoutingConstants;
import com.alibaba.cloud.routing.gateway.constants.RoutingGatewayConstants;
import com.alibaba.cloud.routing.gateway.context.RoutingGatewayContext;
import com.alibaba.cloud.routing.gateway.filter.RoutingGatewayFilter;
import com.alibaba.cloud.routing.gateway.util.RoutingGatewayFilterResolver;
import com.alibaba.cloud.routing.properties.RoutingProperties;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class DefaultRoutingGatewayFilter implements RoutingGatewayFilter {

	// Filter order.
	@Value("${" + RoutingGatewayConstants.GATEWAY_ROUTE_FILTER_ORDER + ":"
			+ RoutingGatewayConstants.GATEWAY_ROUTE_FILTER_ORDER_VALUE + "}")
	protected Integer filterOrder;

	// Gateway rule priority switch.
	@Value("${" + RoutingGatewayConstants.GATEWAY_HEADER_PRIORITY + ":true}")
	protected Boolean gatewayHeaderPriority;

	@Resource
	private RoutingProperties routingProperties;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		RoutingGatewayContext.getCurrentContext().setExchange(exchange);

		ServerHttpRequest request = exchange.getRequest();
		ServerHttpRequest.Builder requestBuilder = request.mutate();

		applyHeader(request, requestBuilder);

		ServerHttpRequest newRequest = requestBuilder.build();
		ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

		RoutingGatewayContext.getCurrentContext().setExchange(newExchange);

		return chain.filter(newExchange);
	}

	private void applyHeader(ServerHttpRequest request,
			ServerHttpRequest.Builder requestBuilder) {

		// Use map to simplify if... else statement
		Map<String, String> propertiesMap = new HashMap<>();
		propertiesMap.put(RoutingConstants.SCA_ROUTING_SERVICE_ZONE,
				routingProperties.getZone());
		RoutingGatewayContext.getCurrentContext().setZone(routingProperties.getZone());
		propertiesMap.put(RoutingConstants.SCA_ROUTING_SERVICE_REGION,
				routingProperties.getRegion());
		RoutingGatewayContext.getCurrentContext()
				.setRegion(routingProperties.getRegion());
		RoutingGatewayContext.getCurrentContext().setServerHttpRequest(request);

		propertiesMap.forEach((k, v) -> RoutingGatewayFilterResolver.setHeader(request,
				requestBuilder, k, v, gatewayHeaderPriority));

	}

	@Override
	public int getOrder() {

		return filterOrder;
	}

}
