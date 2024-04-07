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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static com.alibaba.cloud.circuitbreaker.sentinel.feign.FeignClientCircuitBreakerRuleIntegrationTest.Application;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * @author freeman
 */
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = Application.class, properties = {
		"server.port=10101",
		"spring.cloud.openfeign.circuitbreaker.enabled=true",
		"feign.sentinel.default-rule=default",
		"feign.sentinel.rules.default[0].grade=2",
		"feign.sentinel.rules.default[0].count=2",
		"feign.sentinel.rules.default[0].timeWindow=1",
		"feign.sentinel.rules.default[0].statIntervalMs=30000",
		"feign.sentinel.rules.default[0].minRequestAmount=5",
		"feign.sentinel.rules.user[0].grade=2",
		"feign.sentinel.rules.user[0].count=2",
		"feign.sentinel.rules.user[0].timeWindow=1",
		"feign.sentinel.rules.user[0].statIntervalMs=30000",
		"feign.sentinel.rules.user[0].minRequestAmount=5",
		"feign.sentinel.rules.[user#specificFeignMethod(boolean)][0].grade=2",
		"feign.sentinel.rules.[user#specificFeignMethod(boolean)][0].count=1",
		"feign.sentinel.rules.[user#specificFeignMethod(boolean)][0].timeWindow=1",
		"feign.sentinel.rules.[user#specificFeignMethod(boolean)][0].statIntervalMs=30000",
		"feign.sentinel.rules.[user#specificFeignMethod(boolean)][0].minRequestAmount=5"
})
public class FeignClientCircuitBreakerRuleIntegrationTest {

	@Autowired
	private Application.UserClient userClient;
	@Autowired
	private Application.OrderClient orderClient;

	@Test
	public void testDefaultRule() throws Exception {
		// test default configuration is working

		// ok
		assertThat(orderClient.defaultConfig(true)).isEqualTo("ok");
		assertThat(orderClient.defaultConfig(true)).isEqualTo("ok");
		assertThat(orderClient.defaultConfig(true)).isEqualTo("ok");
		assertThat(orderClient.defaultConfig(true)).isEqualTo("ok");
		assertThat(orderClient.defaultConfig(true)).isEqualTo("ok");

		// occur exception
		assertThat(orderClient.defaultConfig(false)).isEqualTo("fallback");
		assertThat(orderClient.defaultConfig(false)).isEqualTo("fallback");
		// the 3rd exception, circuit breaker open
		assertThat(orderClient.defaultConfig(false)).isEqualTo("fallback");

		// sleep 300, ensure circuit breaker status is open.
		Thread.sleep(300);

		// test circuit breaker open
		assertThat(orderClient.defaultConfig(true)).isEqualTo("fallback");
		assertThat(orderClient.defaultConfig(false)).isEqualTo("fallback");

		// longer than timeWindow, circuit breaker half open
		Thread.sleep(1200L);

		// let circuit breaker close
		assertThat(orderClient.defaultConfig(true)).isEqualTo("ok");
		assertThat(orderClient.defaultConfig(false)).isEqualTo("fallback");
	}

	@Test
	public void testSpecificFeignRule() throws Exception {
		// test specific Feign client configuration is working

		// ok
		assertThat(userClient.specificFeign(true)).isEqualTo("ok");
		assertThat(userClient.specificFeign(true)).isEqualTo("ok");
		assertThat(userClient.specificFeign(true)).isEqualTo("ok");
		assertThat(userClient.specificFeign(true)).isEqualTo("ok");
		assertThat(userClient.specificFeign(true)).isEqualTo("ok");

		// occur exception
		assertThat(userClient.specificFeign(false)).isEqualTo("fallback");
		assertThat(userClient.specificFeign(false)).isEqualTo("fallback");
		// the 3rd exception, circuit breaker open
		assertThat(userClient.specificFeign(false)).isEqualTo("fallback");

		Thread.sleep(300);

		// test circuit breaker open
		assertThat(userClient.specificFeign(true)).isEqualTo("fallback");
		assertThat(userClient.specificFeign(false)).isEqualTo("fallback");

		// longer than timeWindow, circuit breaker half open
		Thread.sleep(1200L);

		// let circuit breaker close
		assertThat(userClient.specificFeign(true)).isEqualTo("ok");
		assertThat(userClient.specificFeign(false)).isEqualTo("fallback");
	}

	@Test
	public void testSpecificFeignMethodRule() throws Exception {
		// test specific Feign client method configuration is working

		// ok
		assertThat(userClient.specificFeignMethod(true)).isEqualTo("ok");
		assertThat(userClient.specificFeignMethod(true)).isEqualTo("ok");
		assertThat(userClient.specificFeignMethod(true)).isEqualTo("ok");
		assertThat(userClient.specificFeignMethod(true)).isEqualTo("ok");
		assertThat(userClient.specificFeignMethod(true)).isEqualTo("ok");

		// occur exception
		assertThat(userClient.specificFeignMethod(false)).isEqualTo("fallback");
		// occur the 2nd exception, circuit breaker open
		assertThat(userClient.specificFeignMethod(false)).isEqualTo("fallback");

		Thread.sleep(300);

		// test circuit breaker is open
		assertThat(userClient.specificFeignMethod(true)).isEqualTo("fallback");
		assertThat(userClient.specificFeignMethod(false)).isEqualTo("fallback");

		// longer than timeWindow, circuit breaker half open
		Thread.sleep(1200L);

		// let circuit breaker close
		assertThat(userClient.specificFeignMethod(true)).isEqualTo("ok");
		assertThat(userClient.specificFeignMethod(false)).isEqualTo("fallback");
	}

	@Configuration
	@EnableAutoConfiguration
	@RestController
	@EnableFeignClients
	protected static class Application {

		@FeignClient(value = "user", url = "http://localhost:${server.port}", fallback = UserClientFallback.class)
		interface UserClient {

			@GetMapping("/specificFeign/{success}")
			String specificFeign(@PathVariable boolean success);

			@GetMapping("/specificFeignMethod/{success}")
			String specificFeignMethod(@PathVariable boolean success);

		}

		@FeignClient(value = "order", url = "http://localhost:${server.port}", fallback = OrderClientFallback.class)
		interface OrderClient {

			@GetMapping("/defaultConfig/{success}")
			String defaultConfig(@PathVariable boolean success);

		}

		@Component
		static class UserClientFallback implements UserClient {

			@Override
			public String specificFeign(boolean success) {
				return "fallback";
			}

			@Override
			public String specificFeignMethod(boolean success) {
				return "fallback";
			}

		}

		@Component
		static class OrderClientFallback implements OrderClient {

			@Override
			public String defaultConfig(boolean success) {
				return "fallback";
			}

		}

		@RestController
		static class TestController {

			@GetMapping("/specificFeign/{success}")
			public String specificFeign(@PathVariable boolean success) {
				if (success) {
					return "ok";
				}
				throw new RuntimeException("failed");
			}

			@GetMapping("/defaultConfig/{success}")
			String defaultConfig(@PathVariable boolean success) {
				if (success) {
					return "ok";
				}
				throw new RuntimeException("failed");
			}

			@GetMapping("/specificFeignMethod/{success}")
			String specificFeignMethod(@PathVariable boolean success) {
				if (success) {
					return "ok";
				}
				throw new RuntimeException("failed");
			}

		}

	}

}
