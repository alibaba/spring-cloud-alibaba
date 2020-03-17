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
import java.util.function.Function;

import com.alibaba.cloud.circuitbreaker.sentinel.SentinelConfigBuilder.SentinelCircuitBreakerConfiguration;
import com.alibaba.csp.sentinel.EntryType;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.util.Assert;

/**
 * @author Eric Zhao
 */
public class SentinelCircuitBreakerFactory extends
		CircuitBreakerFactory<SentinelCircuitBreakerConfiguration, SentinelConfigBuilder> {

	private Function<String, SentinelConfigBuilder.SentinelCircuitBreakerConfiguration> defaultConfiguration = id -> new SentinelConfigBuilder()
			.resourceName(id).entryType(EntryType.OUT).rules(new ArrayList<>()).build();

	@Override
	public CircuitBreaker create(String id) {
		Assert.hasText(id, "A CircuitBreaker must have an id.");
		SentinelConfigBuilder.SentinelCircuitBreakerConfiguration conf = getConfigurations()
				.computeIfAbsent(id, defaultConfiguration);
		return new SentinelCircuitBreaker(id, conf.getEntryType(), conf.getRules());
	}

	@Override
	protected SentinelConfigBuilder configBuilder(String id) {
		return new SentinelConfigBuilder(id);
	}

	@Override
	public void configureDefault(
			Function<String, SentinelCircuitBreakerConfiguration> defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

}
