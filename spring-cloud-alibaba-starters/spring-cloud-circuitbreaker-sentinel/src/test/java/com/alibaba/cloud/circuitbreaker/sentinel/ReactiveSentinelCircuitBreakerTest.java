/*
 * Copyright 2013-present the original author or authors.
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
import java.util.Arrays;
import java.util.Collections;

import com.alibaba.cloud.circuitbreaker.sentinel.feign.CircuitBreakerRuleChangeListener;
import com.alibaba.cloud.circuitbreaker.sentinel.feign.SentinelFeignClientProperties;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Zhao
 */
public class ReactiveSentinelCircuitBreakerTest {

	@AfterEach
	public void tearDown() {
		DegradeRuleManager.loadRules(new ArrayList<>());
	}

	@Test
	public void testDefaultRulesAreIsolatedPerResource() {
		String firstResource = "testReactiveDefaultRulesAreIsolatedPerResourceA";
		String secondResource = "testReactiveDefaultRulesAreIsolatedPerResourceB";
		DegradeRule defaultRule = new DegradeRule().setCount(100).setTimeWindow(10);
		SentinelFeignClientProperties properties = new SentinelFeignClientProperties();
		properties.setRules(Collections.singletonMap(properties.getDefaultRule(),
				Collections.singletonList(defaultRule)));
		ReactiveSentinelCircuitBreakerFactory factory = new ReactiveSentinelCircuitBreakerFactory();

		CircuitBreakerRuleChangeListener.configureDefault(properties, factory);
		factory.create(firstResource);
		factory.create(secondResource);

		assertThat(DegradeRuleManager.hasConfig(firstResource)).isTrue();
		assertThat(DegradeRuleManager.hasConfig(secondResource)).isTrue();
		assertThat(defaultRule.getResource()).isNull();
	}

	@Test
	public void testCreateWithNullRule() {
		String id = "testCreateReactiveCbWithNullRule";
		ReactiveSentinelCircuitBreaker cb = new ReactiveSentinelCircuitBreaker(id,
				Collections.singletonList(null));
		assertThat(Mono.just("foobar").transform(it -> cb.run(it)).block())
				.isEqualTo("foobar");
		assertThat(DegradeRuleManager.hasConfig(id)).isFalse();
	}

	@Test
	public void runMono() {
		ReactiveCircuitBreaker cb = new ReactiveSentinelCircuitBreakerFactory()
				.create("foo");
		assertThat(Mono.just("foobar").transform(it -> cb.run(it)).block())
				.isEqualTo("foobar");
	}

	@Test
	public void runMonoWithFallback() {
		ReactiveCircuitBreaker cb = new ReactiveSentinelCircuitBreakerFactory()
				.create("foo");
		assertThat(Mono.error(new RuntimeException("boom"))
				.transform(it -> cb.run(it, t -> Mono.just("fallback"))).block())
						.isEqualTo("fallback");
	}

	@Test
	public void runFlux() {
		ReactiveCircuitBreaker cb = new ReactiveSentinelCircuitBreakerFactory()
				.create("foo");
		assertThat(Flux.just("foobar", "hello world").transform(it -> cb.run(it))
				.collectList().block()).isEqualTo(Arrays.asList("foobar", "hello world"));
	}

	@Test
	public void runFluxWithFallback() {
		ReactiveCircuitBreaker cb = new ReactiveSentinelCircuitBreakerFactory()
				.create("foo");
		assertThat(Flux.error(new RuntimeException("boom"))
				.transform(it -> cb.run(it, t -> Flux.just("fallback"))).collectList()
				.block()).isEqualTo(Arrays.asList("fallback"));
	}

}
