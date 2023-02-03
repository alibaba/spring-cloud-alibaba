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

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ConfigBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author 黄学敏（huangxuemin)
 */
public class FallbackSupportFactoryBeanTests {

	public static final String FACTORY_BEAN_FALLBACK_MESSAGE = "factoryBean fallback message";

	public static final String ORIGINAL_FALLBACK_MESSAGE = "OriginalFeign fallback message";

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withBean(FactoryBeanFallbackFeignFallback.class).withBean(OriginalFeignFallback.class)
			.withConfiguration(AutoConfigurations.of(TestConfiguration.class, FeignAutoConfiguration.class))
			.withBean(CircuitBreakerFactory.class, () -> new CircuitBreakerFactory() {
				@Override
				public CircuitBreaker create(String id) {
					return new CircuitBreaker() {
						@Override
						public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
							try {
								return toRun.get();
							}
							catch (Throwable t) {
								return fallback.apply(t);
							}
						}
					};
				}

				@Override
				protected ConfigBuilder configBuilder(String id) {
					return null;
				}

				@Override
				public void configureDefault(Function defaultConfiguration) {

				}
			}).withPropertyValues("spring.cloud.openfeign.circuitbreaker.enabled=true");

	@Test
	public void factoryBeanFallback() {

		runner.run(ctx -> {
			assertThat(ctx.getBean(OriginalFeign.class).get()).isEqualTo(ORIGINAL_FALLBACK_MESSAGE);
			assertThat(ctx.getBean(FactoryBeanFallbackFeign.class).get()).isEqualTo(FACTORY_BEAN_FALLBACK_MESSAGE);
		});

	}

	@Configuration(proxyBeanMethods = false)
	@EnableFeignClients(clients = { FallbackSupportFactoryBeanTests.OriginalFeign.class,
			FallbackSupportFactoryBeanTests.FactoryBeanFallbackFeign.class })
	@EnableAutoConfiguration
	public static class TestConfiguration {

	}

	@FeignClient(name = "original", url = "https://original", fallback = OriginalFeignFallback.class)
	public interface OriginalFeign {

		@GetMapping("/")
		String get();

	}

	@FeignClient(name = "factoryBean", url = "https://factoryBean", fallback = FactoryBeanFallbackFeignFallback.class)
	public interface FactoryBeanFallbackFeign {

		@GetMapping("/")
		String get();

	}

	public static class FactoryBeanFallbackFeignFallback implements FactoryBean<FactoryBeanFallbackFeign> {

		@Override
		public FactoryBeanFallbackFeign getObject() throws Exception {
			return new FactoryBeanFallbackFeign() {
				@Override
				public String get() {
					return FACTORY_BEAN_FALLBACK_MESSAGE;
				}
			};
		}

		@Override
		public Class<?> getObjectType() {
			return FactoryBeanFallbackFeign.class;
		}

	}

	public static class OriginalFeignFallback implements OriginalFeign {

		@Override
		public String get() {
			return ORIGINAL_FALLBACK_MESSAGE;
		}

	}

}