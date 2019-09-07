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

import com.alibaba.csp.sentinel.SphU;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for {@link SentinelCircuitBreaker}.
 *
 * @author Eric Zhao
 */
@Configuration
@ConditionalOnClass({ SphU.class })
public class SentinelCircuitBreakerAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(CircuitBreakerFactory.class)
	public CircuitBreakerFactory sentinelCircuitBreakerFactory() {
		return new SentinelCircuitBreakerFactory();
	}

	@Configuration
	public static class SentinelCustomizerConfiguration {

		@Autowired(required = false)
		private List<Customizer<SentinelCircuitBreakerFactory>> customizers = new ArrayList<>();

		@Autowired(required = false)
		private SentinelCircuitBreakerFactory factory;

		@PostConstruct
		public void init() {
			customizers.forEach(customizer -> customizer.customize(factory));
		}

	}

}