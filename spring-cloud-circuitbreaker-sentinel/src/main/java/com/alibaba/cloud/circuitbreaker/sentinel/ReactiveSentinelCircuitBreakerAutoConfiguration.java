/*
 * Copyright 2013-2019 the original author or authors.
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

package com.alibaba.cloud.circuitbreaker.sentinel;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eric Zhao
 */
@Configuration
@ConditionalOnClass(
		name = { "reactor.core.publisher.Mono", "reactor.core.publisher.Flux" })
public class ReactiveSentinelCircuitBreakerAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(ReactiveCircuitBreakerFactory.class)
	public ReactiveCircuitBreakerFactory reactiveSentinelCircuitBreakerFactory() {
		return new ReactiveSentinelCircuitBreakerFactory();
	}

	@Configuration
	@ConditionalOnClass(
			name = { "reactor.core.publisher.Mono", "reactor.core.publisher.Flux" })
	public static class ReactiveSentinelCustomizerConfiguration {

		@Autowired(required = false)
		private List<Customizer<ReactiveSentinelCircuitBreakerFactory>> customizers = new ArrayList<>();

		@Autowired(required = false)
		private ReactiveSentinelCircuitBreakerFactory factory;

		@PostConstruct
		public void init() {
			customizers.forEach(customizer -> customizer.customize(factory));
		}

	}

}
