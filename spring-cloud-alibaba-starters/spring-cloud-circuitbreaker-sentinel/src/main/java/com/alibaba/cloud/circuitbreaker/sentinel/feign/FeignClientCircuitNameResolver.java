package com.alibaba.cloud.circuitbreaker.sentinel.feign;

import java.lang.reflect.Method;
import java.util.Map;

import feign.Feign;
import feign.Target;

import org.springframework.cloud.client.circuitbreaker.AbstractCircuitBreakerFactory;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;

import static com.alibaba.cloud.circuitbreaker.sentinel.feign.CircuitBreakerRuleChangeListener.*;

/**
 * Feign client circuit breaker name resolver.
 *
 * <p>
 * <strong>note:</strong> spring cloud openfeign version need greater than 3.0.4.
 *
 * @author freeman
 * @see CircuitBreakerNameResolver
 */
public class FeignClientCircuitNameResolver implements CircuitBreakerNameResolver {

	private final Map configurations;

	public FeignClientCircuitNameResolver(AbstractCircuitBreakerFactory factory) {
		configurations = getConfigurations(factory);
	}

	@Override
	public String resolveCircuitBreakerName(String feignClientName, Target<?> target,
			Method method) {
		String key = getKey(feignClientName, target, method);

		if (configurations != null && configurations.containsKey(key)) {
			return key;
		}

		return feignClientName;
	}

	private String getKey(String feignClientName, Target<?> target, Method method) {
		String key = Feign.configKey(target.type(), method);
		return feignClientName + key.substring(key.indexOf('#'));
	}

}
