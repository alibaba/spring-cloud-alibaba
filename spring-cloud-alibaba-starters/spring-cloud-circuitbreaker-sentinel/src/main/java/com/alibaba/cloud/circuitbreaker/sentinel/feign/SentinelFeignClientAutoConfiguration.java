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

package com.alibaba.cloud.circuitbreaker.sentinel.feign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.alibaba.cloud.circuitbreaker.sentinel.ReactiveSentinelCircuitBreakerFactory;
import com.alibaba.cloud.circuitbreaker.sentinel.SentinelCircuitBreakerFactory;
import com.alibaba.cloud.circuitbreaker.sentinel.SentinelConfigBuilder;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import feign.Feign;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.AbstractCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for feign client circuit breaker rules.
 *
 * @author freeman
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ Feign.class, FeignClientFactoryBean.class })
@ConditionalOnProperty(name = "spring.cloud.circuitbreaker.sentinel.enabled",
		havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SentinelFeignClientProperties.class)
public class SentinelFeignClientAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(CircuitBreakerNameResolver.class)
	public static class CircuitBreakerNameResolverConfiguration {

		@Bean
		@ConditionalOnMissingBean(CircuitBreakerNameResolver.class)
		public CircuitBreakerNameResolver feignClientCircuitNameResolver(
				ObjectProvider<List<AbstractCircuitBreakerFactory>> provider) {
			List<AbstractCircuitBreakerFactory> factories = provider
					.getIfAvailable(Collections::emptyList);
			if (factories.size() >= 1) {
				return new FeignClientCircuitNameResolver(factories.get(0));
			}
			throw new IllegalArgumentException(
					"need one CircuitBreakerFactory/ReactiveCircuitBreakerFactory, but 0 found.");
		}

	}

	@Configuration(proxyBeanMethods = false)
	public static class SentinelCustomizerConfiguration {

		@Bean
		public Customizer<SentinelCircuitBreakerFactory> configureRulesCustomizer(
				SentinelFeignClientProperties properties) {
			return factory -> {
				configureDefault(properties, factory);
				configureCustom(properties, factory);
			};
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(name = { "reactor.core.publisher.Mono",
			"reactor.core.publisher.Flux" })
	public static class ReactiveSentinelCustomizerConfiguration {

		@Bean
		public Customizer<ReactiveSentinelCircuitBreakerFactory> reactiveConfigureRulesCustomizer(
				SentinelFeignClientProperties properties) {
			return factory -> {
				configureDefault(properties, factory);
				configureCustom(properties, factory);
			};
		}

	}

	private static void configureCustom(SentinelFeignClientProperties properties,
			AbstractCircuitBreakerFactory factory) {
		properties.getRules().forEach((resourceName, degradeRules) -> {
			if (!Objects.equals(properties.getDefaultRule(), resourceName)) {
				factory.configure(builder -> ((SentinelConfigBuilder) builder)
						.rules(properties.getRules().getOrDefault(resourceName,
								new ArrayList<>())),
						resourceName);
			}
		});
	}

	private static void configureDefault(SentinelFeignClientProperties properties,
			AbstractCircuitBreakerFactory factory) {
		List<DegradeRule> defaultConfigurations = properties.getRules()
				.getOrDefault(properties.getDefaultRule(), new ArrayList<>());
		factory.configureDefault(
				resourceName -> new SentinelConfigBuilder(resourceName.toString())
						.entryType(EntryType.OUT).rules(defaultConfigurations).build());
	}

}
