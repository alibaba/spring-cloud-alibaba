/*
 * Copyright 2013-2018 the original author or authors.
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

import com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorTransformer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@RestController
public class SentinelWebFluxController {

	@Autowired
	private ReactiveCircuitBreakerFactory circuitBreakerFactory;

	@GetMapping("/mono")
	public Mono<String> mono() {
		return Mono.just("simple string")
				// transform the publisher here.
				.transform(new SentinelReactorTransformer<>("mono"));
	}

	@GetMapping("/test")
	public Mono<String> test() {
		return Mono.just("simple string")
				// transform the publisher here.
				.transform(new SentinelReactorTransformer<>("test"));
	}

	@GetMapping("/flux")
	public Flux<String> flux() {
		return Flux.fromArray(new String[] { "a", "b", "c" })
				// transform the publisher here.
				.transform(new SentinelReactorTransformer<>("flux"));
	}

	@GetMapping("/cbSlow")
	public Mono<String> cbSlow() {
		int delaySecs = 2;
		return WebClient.builder().baseUrl("http://httpbin.org/").build().get()
				.uri("/delay/" + delaySecs).retrieve().bodyToMono(String.class)
				.transform(it -> circuitBreakerFactory.create("slow_mono").run(it, t -> {
					t.printStackTrace();
					return Mono.just("fallback");
				}));
	}

	@GetMapping("/cbError")
	public Mono<String> cbError() {
		String code = "500";
		return WebClient.builder().baseUrl("http://httpbin.org/").build().get()
				.uri("/status/" + code).retrieve().bodyToMono(String.class)
				.transform(it -> circuitBreakerFactory.create("cbError").run(it, t -> {
					t.printStackTrace();
					return Mono.just("fallback");
				}));
	}

}
