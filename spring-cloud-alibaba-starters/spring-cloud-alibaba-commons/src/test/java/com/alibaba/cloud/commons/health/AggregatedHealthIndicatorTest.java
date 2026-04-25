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

package com.alibaba.cloud.commons.health;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AggregatedHealthIndicatorTest {

	private AggregatedHealthIndicator indicator;

	@AfterEach
	void tearDown() {
		if (indicator != null) {
			indicator.shutdown();
		}
	}

	@Test
	void testAllComponentsUp() {
		indicator = AggregatedHealthIndicator.builder()
				.withNacosCheck(() -> true)
				.withSentinelCheck(() -> true)
				.withRocketMQCheck(() -> true)
				.build();

		Map<String, Object> health = indicator.checkHealth();
		assertEquals(AggregatedHealthIndicator.STATUS_UP, health.get("status"));
		assertNotNull(health.get("components"));
	}

	@Test
	void testOneComponentDown() {
		indicator = AggregatedHealthIndicator.builder()
				.withNacosCheck(() -> true)
				.withSentinelCheck(() -> false)
				.build();

		Map<String, Object> health = indicator.checkHealth();
		assertEquals(AggregatedHealthIndicator.STATUS_DOWN, health.get("status"));

		@SuppressWarnings("unchecked")
		Map<String, Object> components = (Map<String, Object>) health.get("components");
		assertNotNull(components);

		@SuppressWarnings("unchecked")
		Map<String, Object> sentinelHealth = (Map<String, Object>) components.get("sentinel");
		assertEquals(AggregatedHealthIndicator.STATUS_DOWN, sentinelHealth.get("status"));
	}

	@Test
	void testCustomCheck() {
		indicator = AggregatedHealthIndicator.builder()
				.withCustomCheck("custom", () -> AggregatedHealthIndicator.HealthResult.up())
				.build();

		Map<String, Object> health = indicator.checkHealth();
		assertEquals(AggregatedHealthIndicator.STATUS_UP, health.get("status"));
	}

	@Test
	void testCheckComponentHealthExists() {
		indicator = AggregatedHealthIndicator.builder()
				.withNacosCheck(() -> true)
				.build();

		Map<String, Object> nacos = indicator.checkComponentHealth("nacos");
		assertNotNull(nacos);
		assertEquals(AggregatedHealthIndicator.STATUS_UP, nacos.get("status"));
	}

	@Test
	void testCheckComponentHealthNotExists() {
		indicator = AggregatedHealthIndicator.builder()
				.withNacosCheck(() -> true)
				.build();

		Map<String, Object> result = indicator.checkComponentHealth("nonexistent");
		assertNull(result);
	}

	@Test
	void testCaching() {
		AtomicInteger callCount = new AtomicInteger(0);
		indicator = AggregatedHealthIndicator.builder()
				.withNacosCheck(() -> {
					callCount.incrementAndGet();
					return true;
				})
				.cacheTtl(Duration.ofSeconds(10))
				.build();

		indicator.checkHealth();
		indicator.checkHealth();
		assertEquals(1, callCount.get());
	}

	@Test
	void testCacheClear() {
		AtomicInteger callCount = new AtomicInteger(0);
		indicator = AggregatedHealthIndicator.builder()
				.withNacosCheck(() -> {
					callCount.incrementAndGet();
					return true;
				})
				.cacheTtl(Duration.ofSeconds(10))
				.build();

		indicator.checkHealth();
		indicator.clearCache();
		indicator.checkHealth();
		assertEquals(2, callCount.get());
	}

	@Test
	void testTimeout() {
		indicator = AggregatedHealthIndicator.builder()
				.withCustomCheck("slow", () -> {
					Thread.sleep(5000);
					return AggregatedHealthIndicator.HealthResult.up();
				})
				.checkTimeout(Duration.ofMillis(100))
				.degradationEnabled(false)
				.build();

		Map<String, Object> health = indicator.checkHealth();
		@SuppressWarnings("unchecked")
		Map<String, Object> components = (Map<String, Object>) health.get("components");
		@SuppressWarnings("unchecked")
		Map<String, Object> slowHealth = (Map<String, Object>) components.get("slow");
		assertEquals(AggregatedHealthIndicator.STATUS_UNKNOWN, slowHealth.get("status"));
	}

	@Test
	void testTimeoutWithDegradation() {
		indicator = AggregatedHealthIndicator.builder()
				.withCustomCheck("slow", () -> {
					Thread.sleep(5000);
					return AggregatedHealthIndicator.HealthResult.up();
				})
				.checkTimeout(Duration.ofMillis(100))
				.degradationEnabled(true)
				.build();

		Map<String, Object> health = indicator.checkHealth();
		assertEquals(AggregatedHealthIndicator.STATUS_UP, health.get("status"));
	}

	@Test
	void testCircuitBreaker() {
		AggregatedHealthIndicator.CircuitBreaker cb = new AggregatedHealthIndicator.CircuitBreaker(
				3, Duration.ofSeconds(60));

		assertFalse(cb.isOpen());
		cb.recordFailure();
		cb.recordFailure();
		assertFalse(cb.isOpen());
		cb.recordFailure();
		assertTrue(cb.isOpen());
	}

	@Test
	void testCircuitBreakerReset() {
		AggregatedHealthIndicator.CircuitBreaker cb = new AggregatedHealthIndicator.CircuitBreaker(
				2, Duration.ofMillis(50));

		cb.recordFailure();
		cb.recordFailure();
		assertTrue(cb.isOpen());

		cb.recordSuccess();
		assertFalse(cb.isOpen());
		assertEquals(0, cb.getFailureCount());
	}

	@Test
	void testHealthResultFactory() {
		AggregatedHealthIndicator.HealthResult up = AggregatedHealthIndicator.HealthResult.up();
		assertTrue(up.isHealthy());
		assertNull(up.getDetails());

		AggregatedHealthIndicator.HealthResult down = AggregatedHealthIndicator.HealthResult.down();
		assertFalse(down.isHealthy());
	}

	@Test
	void testExceptionInHealthCheck() {
		indicator = AggregatedHealthIndicator.builder()
				.withCustomCheck("failing", () -> {
					throw new RuntimeException("Connection refused");
				})
				.build();

		Map<String, Object> health = indicator.checkHealth();
		assertEquals(AggregatedHealthIndicator.STATUS_DOWN, health.get("status"));
	}

	@Test
	void testBuilderRequiresAtLeastOneCheck() {
		assertThrows(IllegalStateException.class,
				() -> AggregatedHealthIndicator.builder().build());
	}

}
