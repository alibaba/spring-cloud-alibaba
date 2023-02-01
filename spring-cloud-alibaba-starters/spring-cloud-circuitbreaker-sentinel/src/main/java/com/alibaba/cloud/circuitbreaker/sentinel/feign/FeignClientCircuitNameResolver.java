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

package com.alibaba.cloud.circuitbreaker.sentinel.feign;

import java.lang.reflect.Method;
import java.util.Map;

import feign.Feign;
import feign.Target;

import org.springframework.cloud.client.circuitbreaker.AbstractCircuitBreakerFactory;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;

import static com.alibaba.cloud.circuitbreaker.sentinel.feign.CircuitBreakerRuleChangeListener.getConfigurations;

/**
 * Feign client circuit breaker name resolver.
 *
 * @author freeman
 * @since 2021.0.1.0
 * @see CircuitBreakerNameResolver
 */
@SuppressWarnings("rawtypes")
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
