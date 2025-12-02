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

package com.alibaba.cloud.circuitbreaker.sentinel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * @author Eric Zhao
 */
@SpringBootTest(webEnvironment = DEFINED_PORT,
		classes = SentinelCircuitBreakerIntegrationTest.Application.class,
		properties = {"spring.cloud.discovery.client.health-indicator.enabled=false", "local.server.port=7777"})
public class SentinelCircuitBreakerIntegrationTest {

	@Autowired
	private Application.DemoControllerService service;

	@Test
	public void testSlow() throws Exception {
		assertThat(service.slow(true)).isEqualTo("slow");
		assertThat(service.slow(true)).isEqualTo("slow");
		assertThat(service.slow(true)).isEqualTo("slow");
		assertThat(service.slow(false)).isEqualTo("slow");
		assertThat(service.slow(false)).isEqualTo("slow");
		assertThat(service.slow(true)).isEqualTo("slow");
		assertThat(service.slow(true)).isEqualTo("slow");

		// Then in the next 10s, the fallback method should be called.
		for (int i = 0; i < 5; i++) {
			assertThat(service.slow(true)).isEqualTo("fallback");
			Thread.sleep(1000);
		}

		// Try a normal request.
		assertThat(service.slow(false)).isEqualTo("slow");
		// Recovered.
		assertThat(service.slow(true)).isEqualTo("slow");
	}

	@Test
	public void testNormal() {
		assertThat(service.normal()).isEqualTo("normal");
	}

	@BeforeEach
	public void setUp() {
		DegradeRuleManager.loadRules(new ArrayList<>());
	}

	@BeforeEach
	public void tearDown() {
		DegradeRuleManager.loadRules(new ArrayList<>());
	}

	@Configuration
	@EnableAutoConfiguration
	@RestController
	protected static class Application {

		@GetMapping("/slow")
		public String slow(@RequestParam(required = false) Boolean slow)
				throws InterruptedException {
			if (slow == null || slow) {
				Thread.sleep(80);
			}
			return "slow";
		}

		@GetMapping("/normal")
		public String normal() {
			return "normal";
		}

		@Bean
		public Customizer<SentinelCircuitBreakerFactory> slowCustomizer() {
			String slowId = "slow";
			List<DegradeRule> rules = Collections.singletonList(new DegradeRule(slowId)
					.setGrade(RuleConstant.DEGRADE_GRADE_RT).setCount(50)
					.setSlowRatioThreshold(0.7).setMinRequestAmount(5)
					.setStatIntervalMs(30000).setTimeWindow(5));
			return factory -> {
				factory.configure(builder -> builder.rules(rules), slowId);
				factory.configureDefault(id -> new SentinelConfigBuilder()
						.resourceName(id)
						.rules(Collections.singletonList(new DegradeRule(id)
								.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT)
								.setCount(10).setStatIntervalMs(10000).setTimeWindow(10)))
						.build());
			};
		}

		@Bean
		public RestTestClient restTestClient() {
			// 使用动态注入的端口来构建基础URL
			String baseUrl = "http://localhost:" + 8080;
			return RestTestClient.bindToServer().baseUrl(baseUrl).build();
		}

		@Service
		public static class DemoControllerService {

			private RestTestClient rest;

			private CircuitBreakerFactory cbFactory;

			DemoControllerService(RestTestClient rest,
					CircuitBreakerFactory cbFactory) {
				this.rest = rest;
				this.cbFactory = cbFactory;
			}

			public String slow(boolean slow) {
				return cbFactory.create("slow").run(
						() -> rest.get()
								.uri("/slow?slow=" + slow) // 使用流式 API
								.exchange()
								.expectStatus().isOk()
								.expectBody(String.class)
								.returnResult()
								.getResponseBody(), // 获取响应体
						t -> "fallback");
			}

			public String normal() {
				return cbFactory.create("normal").run(
						() -> rest.get()
								.uri("/normal")
								.exchange()
								.expectStatus().isOk()
								.expectBody(String.class)
								.returnResult()
								.getResponseBody(),
						t -> "fallback");
			}

		}

	}

}
