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
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggregated health indicator for Spring Cloud Alibaba components.
 * Supports Nacos, Sentinel, RocketMQ health checks with circuit breaker
 * pattern, caching, and degradation support.
 *
 * @author Srikanth Patchava
 */
public class AggregatedHealthIndicator {

	private static final Logger log = LoggerFactory.getLogger(AggregatedHealthIndicator.class);

	/** Health status constants. */
	public static final String STATUS_UP = "UP";

	public static final String STATUS_DOWN = "DOWN";

	public static final String STATUS_UNKNOWN = "UNKNOWN";

	public static final String STATUS_DEGRADED = "DEGRADED";

	private final Map<String, HealthCheck> healthChecks;

	private final Map<String, CachedHealth> healthCache;

	private final Map<String, CircuitBreaker> circuitBreakers;

	private final Duration cacheTtl;

	private final Duration checkTimeout;

	private final boolean degradationEnabled;

	private final ExecutorService executor;

	private AggregatedHealthIndicator(Builder builder) {
		this.healthChecks = Collections.unmodifiableMap(new LinkedHashMap<>(builder.healthChecks));
		this.healthCache = new ConcurrentHashMap<>();
		this.circuitBreakers = new ConcurrentHashMap<>();
		this.cacheTtl = builder.cacheTtl;
		this.checkTimeout = builder.checkTimeout;
		this.degradationEnabled = builder.degradationEnabled;
		this.executor = Executors.newFixedThreadPool(
				Math.max(1, builder.healthChecks.size()));

		for (String name : healthChecks.keySet()) {
			circuitBreakers.put(name, new CircuitBreaker(
					builder.failureThreshold,
					builder.circuitBreakerResetTimeout));
		}
	}

	/**
	 * Perform aggregated health check across all registered components.
	 * @return map with overall status and individual component results
	 */
	public Map<String, Object> checkHealth() {
		Map<String, Object> result = new LinkedHashMap<>();
		Map<String, Object> components = new LinkedHashMap<>();
		boolean allUp = true;
		boolean anyDown = false;

		for (Map.Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
			String name = entry.getKey();
			HealthCheck check = entry.getValue();
			Map<String, Object> componentHealth = checkComponent(name, check);
			components.put(name, componentHealth);

			String status = (String) componentHealth.get("status");
			if (!STATUS_UP.equals(status)) {
				allUp = false;
			}
			if (STATUS_DOWN.equals(status)) {
				anyDown = true;
			}
		}

		if (allUp) {
			result.put("status", STATUS_UP);
		}
		else if (anyDown) {
			result.put("status", STATUS_DOWN);
		}
		else {
			result.put("status", STATUS_DEGRADED);
		}
		result.put("components", components);
		return result;
	}

	private Map<String, Object> checkComponent(String name, HealthCheck check) {
		CachedHealth cached = healthCache.get(name);
		if (cached != null && !cached.isExpired(cacheTtl)) {
			return cached.health;
		}

		CircuitBreaker cb = circuitBreakers.get(name);
		if (cb != null && cb.isOpen()) {
			Map<String, Object> degradedHealth = new LinkedHashMap<>();
			if (degradationEnabled) {
				degradedHealth.put("status", STATUS_UP);
				degradedHealth.put("detail", "circuit breaker open - degraded to UP");
			}
			else {
				degradedHealth.put("status", STATUS_UNKNOWN);
				degradedHealth.put("detail", "circuit breaker open");
			}
			return degradedHealth;
		}

		Map<String, Object> health = executeHealthCheck(name, check, cb);
		healthCache.put(name, new CachedHealth(health, Instant.now()));
		return health;
	}

	private Map<String, Object> executeHealthCheck(String name, HealthCheck check,
			CircuitBreaker cb) {
		Future<Map<String, Object>> future = executor.submit(() -> {
			Map<String, Object> h = new LinkedHashMap<>();
			try {
				HealthResult result = check.check();
				h.put("status", result.isHealthy() ? STATUS_UP : STATUS_DOWN);
				if (result.getDetails() != null) {
					h.putAll(result.getDetails());
				}
				if (cb != null) {
					if (result.isHealthy()) {
						cb.recordSuccess();
					}
					else {
						cb.recordFailure();
					}
				}
			}
			catch (Exception e) {
				h.put("status", STATUS_DOWN);
				h.put("error", e.getMessage());
				if (cb != null) {
					cb.recordFailure();
				}
			}
			return h;
		});

		try {
			return future.get(checkTimeout.toMillis(), TimeUnit.MILLISECONDS);
		}
		catch (TimeoutException e) {
			future.cancel(true);
			log.warn("Health check timed out for component: {}", name);
			if (degradationEnabled) {
				Map<String, Object> degraded = new LinkedHashMap<>();
				degraded.put("status", STATUS_UP);
				degraded.put("detail", "check timed out - degraded to UP");
				return degraded;
			}
			Map<String, Object> timeout = new LinkedHashMap<>();
			timeout.put("status", STATUS_UNKNOWN);
			timeout.put("error", "Health check timed out");
			return timeout;
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Map<String, Object> interrupted = new LinkedHashMap<>();
			interrupted.put("status", STATUS_UNKNOWN);
			interrupted.put("error", "Health check interrupted");
			return interrupted;
		}
		catch (ExecutionException e) {
			Map<String, Object> execError = new LinkedHashMap<>();
			execError.put("status", STATUS_DOWN);
			execError.put("error", e.getCause() != null
					? e.getCause().getMessage() : e.getMessage());
			if (cb != null) {
				cb.recordFailure();
			}
			return execError;
		}
	}

	/**
	 * Check health of a single named component.
	 * @param name the component name
	 * @return health details or null if component not registered
	 */
	public Map<String, Object> checkComponentHealth(String name) {
		HealthCheck check = healthChecks.get(name);
		if (check == null) {
			return null;
		}
		return checkComponent(name, check);
	}

	/** Clear all cached health results. */
	public void clearCache() {
		healthCache.clear();
	}

	/** Shutdown the executor service. */
	public void shutdown() {
		executor.shutdown();
		try {
			if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		}
		catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	/** Create a new builder. */
	public static Builder builder() {
		return new Builder();
	}

	// --- Inner classes ---

	/** Functional interface for individual health checks. */
	@FunctionalInterface
	public interface HealthCheck {

		HealthResult check() throws Exception;

	}

	/** Result of a single health check. */
	public static class HealthResult {

		private final boolean healthy;

		private final Map<String, Object> details;

		public HealthResult(boolean healthy) {
			this(healthy, null);
		}

		public HealthResult(boolean healthy, Map<String, Object> details) {
			this.healthy = healthy;
			this.details = details;
		}

		public boolean isHealthy() {
			return healthy;
		}

		public Map<String, Object> getDetails() {
			return details;
		}

		public static HealthResult up() {
			return new HealthResult(true);
		}

		public static HealthResult up(Map<String, Object> details) {
			return new HealthResult(true, details);
		}

		public static HealthResult down() {
			return new HealthResult(false);
		}

		public static HealthResult down(Map<String, Object> details) {
			return new HealthResult(false, details);
		}

	}

	/** Circuit breaker for protecting health check calls. */
	static class CircuitBreaker {

		private final int failureThreshold;

		private final Duration resetTimeout;

		private final AtomicInteger failureCount = new AtomicInteger(0);

		private volatile Instant lastFailureTime = Instant.EPOCH;

		private volatile boolean open = false;

		CircuitBreaker(int failureThreshold, Duration resetTimeout) {
			this.failureThreshold = failureThreshold;
			this.resetTimeout = resetTimeout;
		}

		boolean isOpen() {
			if (!open) {
				return false;
			}
			if (Duration.between(lastFailureTime, Instant.now()).compareTo(resetTimeout) > 0) {
				open = false;
				failureCount.set(0);
				return false;
			}
			return true;
		}

		void recordSuccess() {
			failureCount.set(0);
			open = false;
		}

		void recordFailure() {
			lastFailureTime = Instant.now();
			if (failureCount.incrementAndGet() >= failureThreshold) {
				open = true;
			}
		}

		int getFailureCount() {
			return failureCount.get();
		}

		boolean isCurrentlyOpen() {
			return open;
		}

	}

	/** Cached health check result with timestamp. */
	private static class CachedHealth {

		final Map<String, Object> health;

		final Instant timestamp;

		CachedHealth(Map<String, Object> health, Instant timestamp) {
			this.health = health;
			this.timestamp = timestamp;
		}

		boolean isExpired(Duration ttl) {
			return Duration.between(timestamp, Instant.now()).compareTo(ttl) > 0;
		}

	}

	/** Builder for AggregatedHealthIndicator. */
	public static class Builder {

		private final Map<String, HealthCheck> healthChecks = new LinkedHashMap<>();

		private Duration cacheTtl = Duration.ofSeconds(30);

		private Duration checkTimeout = Duration.ofSeconds(5);

		private boolean degradationEnabled = true;

		private int failureThreshold = 3;

		private Duration circuitBreakerResetTimeout = Duration.ofSeconds(60);

		public Builder withNacosCheck(Supplier<Boolean> nacosChecker) {
			healthChecks.put("nacos", () -> {
				boolean up = nacosChecker.get();
				Map<String, Object> details = new LinkedHashMap<>();
				details.put("component", "nacos");
				return new HealthResult(up, details);
			});
			return this;
		}

		public Builder withSentinelCheck(Supplier<Boolean> sentinelChecker) {
			healthChecks.put("sentinel", () -> {
				boolean up = sentinelChecker.get();
				Map<String, Object> details = new LinkedHashMap<>();
				details.put("component", "sentinel");
				return new HealthResult(up, details);
			});
			return this;
		}

		public Builder withRocketMQCheck(Supplier<Boolean> rocketMQChecker) {
			healthChecks.put("rocketmq", () -> {
				boolean up = rocketMQChecker.get();
				Map<String, Object> details = new LinkedHashMap<>();
				details.put("component", "rocketmq");
				return new HealthResult(up, details);
			});
			return this;
		}

		public Builder withCustomCheck(String name, HealthCheck check) {
			healthChecks.put(name, check);
			return this;
		}

		public Builder cacheTtl(Duration cacheTtl) {
			this.cacheTtl = cacheTtl;
			return this;
		}

		public Builder checkTimeout(Duration checkTimeout) {
			this.checkTimeout = checkTimeout;
			return this;
		}

		public Builder degradationEnabled(boolean degradationEnabled) {
			this.degradationEnabled = degradationEnabled;
			return this;
		}

		public Builder failureThreshold(int failureThreshold) {
			this.failureThreshold = failureThreshold;
			return this;
		}

		public Builder circuitBreakerResetTimeout(Duration resetTimeout) {
			this.circuitBreakerResetTimeout = resetTimeout;
			return this;
		}

		public AggregatedHealthIndicator build() {
			if (healthChecks.isEmpty()) {
				throw new IllegalStateException("At least one health check must be configured");
			}
			return new AggregatedHealthIndicator(this);
		}

	}

}
