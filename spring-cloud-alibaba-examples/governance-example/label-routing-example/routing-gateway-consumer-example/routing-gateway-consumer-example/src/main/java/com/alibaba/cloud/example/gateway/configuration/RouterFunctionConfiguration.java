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

import com.alibaba.cloud.example.gateway.handler.AddRouterRouteRuleHandler;
import com.alibaba.cloud.example.gateway.handler.GetServerListHandler;
import com.alibaba.cloud.example.gateway.handler.UpdateRoutingRuleHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@Configuration
public class RouterFunctionConfiguration {

	@Autowired
	private AddRouterRouteRuleHandler addRouterRouteRuleHandler;

	@Autowired
	private UpdateRoutingRuleHandler updateRoutingRuleHandler;

	@Autowired
	private GetServerListHandler getServerListHandler;

	@SuppressWarnings("rawtypes")
	@Bean
	public RouterFunction routerFunction() {

		return RouterFunctions.route()
				.GET("/add", RequestPredicates.accept(MediaType.TEXT_PLAIN),
						addRouterRouteRuleHandler)
				.GET("/update", RequestPredicates.accept(MediaType.TEXT_PLAIN),
						updateRoutingRuleHandler)
				.GET("/all-service", RequestPredicates.accept(MediaType.TEXT_PLAIN),
						getServerListHandler)
				.build();
	}

}
