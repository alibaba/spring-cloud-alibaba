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

package com.alibaba.cloud.examples;

import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * Example of responsive discovery client.
 *
 * @author fangjian0423, MieAh
 */
@RestController
public class MyController {

	@Resource
	private ReactiveDiscoveryClient reactiveDiscoveryClient;

	@Resource
	private WebClient.Builder webClientBuilder;

	@GetMapping("/all-services")
	public Flux<String> allServices() {
		return reactiveDiscoveryClient.getInstances("service-provider")
				.map(serviceInstance -> serviceInstance.getHost() + ":"
						+ serviceInstance.getPort());
	}

	@GetMapping("/service-call/{name}")
	public Mono<String> serviceCall(@PathVariable("name") String name) {
		return webClientBuilder.build().get()
				.uri("http://service-provider/echo/" + name).retrieve()
				.bodyToMono(String.class);
	}

}
