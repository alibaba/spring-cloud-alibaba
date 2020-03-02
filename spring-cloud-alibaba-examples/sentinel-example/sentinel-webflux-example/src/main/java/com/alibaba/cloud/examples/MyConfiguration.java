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

import java.util.Collections;

import com.alibaba.cloud.circuitbreaker.sentinel.ReactiveSentinelCircuitBreakerFactory;
import com.alibaba.cloud.circuitbreaker.sentinel.SentinelConfigBuilder;
import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import reactor.core.publisher.Mono;

import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration
public class MyConfiguration {

	@Bean
	public BlockRequestHandler blockRequestHandler() {
		return new BlockRequestHandler() {
			@Override
			public Mono<ServerResponse> handleRequest(ServerWebExchange exchange,
					Throwable t) {
				return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
						.contentType(MediaType.APPLICATION_JSON).body(fromValue("block"));
			}
		};
	}

	@Bean
	public Customizer<ReactiveSentinelCircuitBreakerFactory> slowCustomizer() {
		return factory -> {
			factory.configure(builder -> builder.rules(Collections.singletonList(
					new DegradeRule("slow_mono").setGrade(RuleConstant.DEGRADE_GRADE_RT)
							.setCount(100).setTimeWindow(5))),
					"slow_mono");
			factory.configure(builder -> builder.rules(Collections.singletonList(
					new DegradeRule("slow_flux").setGrade(RuleConstant.DEGRADE_GRADE_RT)
							.setCount(100).setTimeWindow(5))),
					"slow_flux");
			factory.configureDefault(id -> new SentinelConfigBuilder().resourceName(id)
					.rules(Collections.singletonList(new DegradeRule(id)
							.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT)
							.setCount(0.5).setTimeWindow(10)))
					.build());
		};
	}

}
