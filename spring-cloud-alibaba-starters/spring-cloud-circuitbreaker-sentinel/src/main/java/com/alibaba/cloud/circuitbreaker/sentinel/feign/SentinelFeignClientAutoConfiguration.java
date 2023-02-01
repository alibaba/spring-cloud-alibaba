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

import java.util.Collections;
import java.util.List;

import com.alibaba.cloud.circuitbreaker.sentinel.ReactiveSentinelCircuitBreakerFactory;
import com.alibaba.cloud.circuitbreaker.sentinel.SentinelCircuitBreakerFactory;
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

import static com.alibaba.cloud.circuitbreaker.sentinel.feign.CircuitBreakerRuleChangeListener.configureCustom;
import static com.alibaba.cloud.circuitbreaker.sentinel.feign.CircuitBreakerRuleChangeListener.configureDefault;

/**
 * Auto configuration for feign client circuit breaker rules.
 *
 * @author freeman
 * @since 2021.0.1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ Feign.class, FeignClientFactoryBean.class })
@ConditionalOnProperty(name = "spring.cloud.circuitbreaker.sentinel.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SentinelFeignClientProperties.class)
public class SentinelFeignClientAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(name = "feign.sentinel.enable-refresh-rules", havingValue = "true", matchIfMissing = true)
	public static class CircuitBreakerListenerConfiguration {

		@Bean
		public CircuitBreakerRuleChangeListener circuitBreakerRuleChangeListener() {
			return new CircuitBreakerRuleChangeListener();
		}

	}

	@Configuration(proxyBeanMethods = false)
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

}
