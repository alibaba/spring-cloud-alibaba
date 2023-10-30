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

package com.alibaba.cloud.consumer.reactive.configuration;

import reactor.core.publisher.Mono;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author yuluo-yx
 * @author <a href="1481556636@qq.com"></a>
 */

@Configuration
public class ConsumerWebClientConfiguration {

	private static String serverPort = null;

	@Bean
	@LoadBalanced
	public WebClient.Builder webClient() {

		return WebClient.builder().filter(response());
	}

	private ExchangeFilterFunction response() {

		return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {

			Object body = clientResponse.body(BodyExtractors.toDataBuffers());
			serverPort = body.toString().substring(45, 50);

			return Mono.just(clientResponse);
		});
	}

	public String getServerPort() {

		return serverPort;
	}

}
