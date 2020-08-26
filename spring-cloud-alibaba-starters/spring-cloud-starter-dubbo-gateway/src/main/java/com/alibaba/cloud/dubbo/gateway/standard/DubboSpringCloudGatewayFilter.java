/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.dubbo.gateway.standard;

import com.alibaba.cloud.dubbo.gateway.DubboCloudGatewayExecutor;
import com.alibaba.cloud.dubbo.gateway.DubboCloudGatewayProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

/**
 * The Spring Cloud {@link GatewayFilter Gateway Filter} for Dubbo
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboSpringCloudGatewayFilter
		implements GatewayFilter, GlobalFilter, Ordered {

	private final Log logger = LogFactory.getLog(getClass());

	private int order;

	private final DubboCloudGatewayExecutor dubboCloudGatewayExecutor;

	public DubboSpringCloudGatewayFilter(
			DubboCloudGatewayExecutor dubboCloudGatewayExecutor) {
		this.dubboCloudGatewayExecutor = dubboCloudGatewayExecutor;
	}

	DubboCloudGatewayProperties properties = new DubboCloudGatewayProperties();

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		properties.setContextPath("");
		Object result = dubboCloudGatewayExecutor.execute(exchange.getRequest());

		if (result == null) {
			chain.filter(exchange);
		}
		else {
			ServerHttpResponse response = exchange.getResponse();
		}

		return Mono.empty();
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return order;
	}

}
