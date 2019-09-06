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
import java.util.Collections;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;

import org.junit.After;
import org.junit.Test;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Zhao
 */
public class SentinelCircuitBreakerTest {

	@After
	public void tearDown() {
		// Clear the rules.
		DegradeRuleManager.loadRules(new ArrayList<>());
	}

	@Test
	public void testCreateDirectlyThenRun() {
		// Create a circuit breaker without any circuit breaking rules.
		CircuitBreaker cb = new SentinelCircuitBreaker(
				"testSentinelCreateDirectlyThenRunA");
		assertThat(cb.run(() -> "Sentinel")).isEqualTo("Sentinel");
		assertThat(DegradeRuleManager.hasConfig("testSentinelCreateDirectlyThenRunA"))
				.isFalse();

		CircuitBreaker cb2 = new SentinelCircuitBreaker(
				"testSentinelCreateDirectlyThenRunB",
				Collections.singletonList(
						new DegradeRule("testSentinelCreateDirectlyThenRunB")
								.setCount(100).setTimeWindow(10)));
		assertThat(cb2.run(() -> "Sentinel")).isEqualTo("Sentinel");
		assertThat(DegradeRuleManager.hasConfig("testSentinelCreateDirectlyThenRunB"))
				.isTrue();
	}

	@Test
	public void testCreateWithNullRule() {
		String id = "testCreateCbWithNullRule";
		CircuitBreaker cb = new SentinelCircuitBreaker(id,
				Collections.singletonList(null));
		assertThat(cb.run(() -> "Sentinel")).isEqualTo("Sentinel");
		assertThat(DegradeRuleManager.hasConfig(id)).isFalse();
	}

	@Test
	public void testCreateFromFactoryThenRun() {
		CircuitBreaker cb = new SentinelCircuitBreakerFactory().create("testSentinelRun");
		assertThat(cb.run(() -> "foobar")).isEqualTo("foobar");
	}

	@Test
	public void testRunWithFallback() {
		CircuitBreaker cb = new SentinelCircuitBreakerFactory()
				.create("testSentinelRunWithFallback");
		assertThat(cb.<String> run(() -> {
			throw new RuntimeException("boom");
		}, t -> "fallback")).isEqualTo("fallback");
	}

}