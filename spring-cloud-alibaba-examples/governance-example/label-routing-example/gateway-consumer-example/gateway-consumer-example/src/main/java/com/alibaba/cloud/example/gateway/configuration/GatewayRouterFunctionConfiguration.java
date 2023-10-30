/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.example.gateway.configuration;

import com.alibaba.cloud.example.gateway.handler.AddGatewayRoutingRuleHandler;
import com.alibaba.cloud.example.gateway.handler.GatewayServerListHandler;
import com.alibaba.cloud.example.gateway.handler.UpdateGatewayRoutingRuleHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

/**
 * @author yuluo-yx
 * @author <a href="1481556636@qq.com"></a>
 */

@Configuration
public class GatewayRouterFunctionConfiguration {

	@Autowired
	private AddGatewayRoutingRuleHandler addGatewayRoutingRuleHandler;

	@Autowired
	private UpdateGatewayRoutingRuleHandler updateGatewayRoutingRuleHandler;

	@Autowired
	private GatewayServerListHandler gatewayServerListHandler;

	@SuppressWarnings("rawtypes")
	@Bean
	public RouterFunction gatewayRouterFunction() {

		return RouterFunctions.route()
				.GET("/addRule", RequestPredicates.accept(MediaType.TEXT_PLAIN),
						addGatewayRoutingRuleHandler)
				.GET("/updateRule", RequestPredicates.accept(MediaType.TEXT_PLAIN),
						updateGatewayRoutingRuleHandler)
				.GET("/services", RequestPredicates.accept(MediaType.TEXT_PLAIN),
						gatewayServerListHandler)
				.build();
	}

}
