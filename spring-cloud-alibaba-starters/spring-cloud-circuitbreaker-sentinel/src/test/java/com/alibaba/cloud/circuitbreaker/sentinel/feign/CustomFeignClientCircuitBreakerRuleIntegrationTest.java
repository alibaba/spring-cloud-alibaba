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

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * @author freeman
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = CustomFeignClientCircuitBreakerRuleIntegrationTest.Application.class, properties = {
		"server.port=10101", "feign.circuitbreaker.enabled=true",
		"spring.cloud.discovery.client.health-indicator.enabled=false",
		"feign.sentinel.default-rule=default",
		"feign.sentinel.rules.default[0].grade=2",
		"feign.sentinel.rules.default[0].count=1",
		"feign.sentinel.rules.default[0].timeWindow=1",
		"feign.sentinel.rules.default[0].statIntervalMs=1000",
		"feign.sentinel.rules.default[0].minRequestAmount=5",
		"feign.sentinel.rules.[UserClient#success(boolean)][0].grade=2",
		"feign.sentinel.rules.[UserClient#success(boolean)][0].count=1",
		"feign.sentinel.rules.[UserClient#success(boolean)][0].timeWindow=1",
		"feign.sentinel.rules.[UserClient#success(boolean)][0].statIntervalMs=1000",
		"feign.sentinel.rules.[UserClient#success(boolean)][0].minRequestAmount=5" })
@DirtiesContext
public class CustomFeignClientCircuitBreakerRuleIntegrationTest {

	@Autowired
	private Application.UserClient userClient;

	@Test
	public void testConfigSpecificRule() throws Exception {
		// test specific configuration is working

		// ok
		assertThat(userClient.success(true)).isEqualTo("ok");

		// occur exception, circuit breaker open
		assertThat(userClient.success(false)).isEqualTo("fallback");
		assertThat(userClient.success(false)).isEqualTo("fallback");
		assertThat(userClient.success(false)).isEqualTo("fallback");
		assertThat(userClient.success(false)).isEqualTo("fallback");
		assertThat(userClient.success(false)).isEqualTo("fallback");

		// test circuit breaker open
		assertThat(userClient.success(true)).isEqualTo("fallback");
		assertThat(userClient.success(true)).isEqualTo("fallback");

		Thread.sleep(1100L);

		// test circuit breaker close
		assertThat(userClient.success(true)).isEqualTo("ok");
	}

	@Test
	public void testConfigDefaultRule() throws Exception {
		// test default configuration is working

		// ok
		assertThat(userClient.defaultConfig(true)).isEqualTo("ok");

		// occur exception, circuit breaker open
		assertThat(userClient.defaultConfig(false)).isEqualTo("fallback");
		assertThat(userClient.defaultConfig(false)).isEqualTo("fallback");
		assertThat(userClient.defaultConfig(false)).isEqualTo("fallback");
		assertThat(userClient.defaultConfig(false)).isEqualTo("fallback");
		assertThat(userClient.defaultConfig(false)).isEqualTo("fallback");

		// test circuit breaker open
		assertThat(userClient.defaultConfig(true)).isEqualTo("fallback");
		assertThat(userClient.defaultConfig(true)).isEqualTo("fallback");

		Thread.sleep(1100L);

		// test circuit breaker close
		assertThat(userClient.defaultConfig(true)).isEqualTo("ok");
	}

	@Before
	public void reset() {
		DegradeRuleManager.loadRules(new ArrayList<>());
	}

	@Configuration
	@EnableAutoConfiguration
	@RestController
	@EnableFeignClients
	protected static class Application {

		@FeignClient(value = "user", url = "http://localhost:${server.port}", fallback = UserClientFallback.class)
		interface UserClient {

			@GetMapping("/{success}")
			String success(@PathVariable boolean success);

			@GetMapping("/default/{success}")
			String defaultConfig(@PathVariable boolean success);
		}

		@Component
		static class UserClientFallback implements UserClient {

			@Override
			public String success(boolean success) {
				return "fallback";
			}

			@Override
			public String defaultConfig(boolean success) {
				return "fallback";
			}
		}

		@RestController
		static class UserController {

			@GetMapping("/{success}")
			public String success(@PathVariable boolean success) {
				if (success) {
					return "ok";
				}
				throw new RuntimeException("failed");
			}

			@GetMapping("/default/{success}")
			String defaultConfig(@PathVariable boolean success) {
				if (success) {
					return "ok";
				}
				throw new RuntimeException("failed");
			}
		}

	}

}
