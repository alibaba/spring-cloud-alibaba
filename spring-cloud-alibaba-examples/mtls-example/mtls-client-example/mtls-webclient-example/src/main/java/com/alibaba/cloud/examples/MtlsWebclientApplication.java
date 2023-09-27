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

package com.alibaba.cloud.examples;

import com.alibaba.cloud.mtls.client.MtlsClientSSLContext;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

@SpringBootApplication
public class MtlsWebclientApplication {

	public static void main(String[] args) {
		SpringApplication.run(MtlsWebclientApplication.class, args);
	}

	@RestController
	public class Controller {

		@Autowired
		MtlsClientSSLContext mtlsClientSSLContext;

		@Autowired
		private WebClient.Builder builder;

		@GetMapping("/webclient/getMvc")
		public Mono<String> getMvc(ServerWebExchange serverWebExchange) {
			return builder.build().get().uri("https://mtls-mvc-example/mvc/get")
					.retrieve().bodyToMono(String.class);
		}

		@GetMapping("/webclient/getWebflux")
		public Mono<String> getWebflux(ServerWebExchange serverWebExchange) {
			return builder.build().get().uri("https://mtls-webflux-example/webflux/get")
					.retrieve().bodyToMono(String.class);
		}

	}

}
