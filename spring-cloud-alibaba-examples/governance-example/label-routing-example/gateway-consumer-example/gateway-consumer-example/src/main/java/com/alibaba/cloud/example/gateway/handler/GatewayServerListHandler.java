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

package com.alibaba.cloud.example.gateway.handler;

import java.util.HashMap;
import java.util.List;

import reactor.core.publisher.Mono;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author yuluo-yx
 * @author <a href="1481556636@qq.com"></a>
 */

@Service
public class GatewayServerListHandler implements HandlerFunction<ServerResponse> {

	private final DiscoveryClient discoveryClient;

	public GatewayServerListHandler(DiscoveryClient discoveryClient) {
		this.discoveryClient = discoveryClient;
	}

	@Override
	public Mono<ServerResponse> handle(ServerRequest serverRequest) {

		HashMap<String, List<ServiceInstance>> map = new HashMap<>();

		List<String> services = discoveryClient.getServices();
		for (String service : services) {
			List<ServiceInstance> instances = discoveryClient.getInstances(service);
			map.put(service, instances);
		}

		return ServerResponse.status(HttpStatus.OK).body(BodyInserters.fromValue(map));

	}

}
