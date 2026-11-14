/*
 * Copyright 2013-2026 the original author or authors.
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

package com.alibaba.cloud.circuitbreaker.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.cloud.circuitbreaker.sentinel.feign.SentinelFeignClientProperties;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.bind.annotation.GetMapping;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RealFeignRefreshTest.App.class, webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = { "spring.cloud.openfeign.circuitbreaker.enabled=true", "feign.sentinel.rules.default[0].grade=2",
				"feign.sentinel.rules.default[0].count=1", "feign.sentinel.rules.default[0].minRequestAmount=1",
				"feign.sentinel.rules.default[0].timeWindow=120",
				"feign.sentinel.rules.default[0].statIntervalMs=300000", "test.client.name=resolvedClient" })
@org.springframework.test.annotation.DirtiesContext(
		classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RealFeignRefreshTest {

	static com.sun.net.httpserver.HttpServer server;
	static AtomicInteger status = new AtomicInteger(500);

	static AtomicInteger hits = new AtomicInteger();

	@DynamicPropertySource
	static void props(DynamicPropertyRegistry r) throws Exception {
		status.set(500);
		hits.set(0);
		server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", ex -> {
			hits.incrementAndGet();
			byte[] b = "live".getBytes();
			ex.sendResponseHeaders(status.get(), b.length);
			ex.getResponseBody().write(b);
			ex.close();
		});
		server.start();
		r.add("test.url", () -> "http://127.0.0.1:" + server.getAddress().getPort());
	}

	@AfterEach
	void stop() {
		server.stop(0);
		DegradeRuleManager.loadRules(new ArrayList<>());
	}

	@Autowired
	AliasContext alias;

	@Autowired
	ValueContext value;

	@Autowired
	Placeholder placeholder;

	@Autowired
	Plain plain;

	@Autowired
	SentinelFeignClientProperties properties;

	@Autowired
	org.springframework.context.ApplicationContext context;

	String invoke(java.util.function.Supplier<String> s) {
		try {
			return s.get();
		}
		catch (Exception e) {
			return "blocked-or-failed:" + e.getClass().getSimpleName();
		}
	}

	@Test
	void aliasContext() {
		actualFeignRefresh("ordersV2", alias::call);
	}

	@Test
	void valueContext() {
		actualFeignRefresh("otherOrdersV2", value::call);
	}

	@Test
	void placeholder() {
		actualFeignRefresh("resolvedClient", placeholder::call);
	}

	@Test
	void plain() {
		actualFeignRefresh("plain", plain::call);
	}

	void actualFeignRefresh(String id, java.util.function.Supplier<String> call) {
		Map<String, java.util.function.Supplier<String>> calls = new LinkedHashMap<>();
		calls.put(id, call);
		for (var c : calls.entrySet()) {
			for (int i = 0; i < 3; i++) {
				invoke(c.getValue());
			}
			System.out
				.println("REAL_FEIGN_BEFORE id=" + c.getKey() + " hasRule=" + DegradeRuleManager.hasConfig(c.getKey()));
			assertThat(DegradeRuleManager.hasConfig(c.getKey())).isTrue();
		}
		status.set(200);
		int before = hits.get();
		for (var c : calls.values()) {
			assertThat(invoke(c)).startsWith("blocked-or-failed");
		}
		assertThat(hits.get()).isEqualTo(before);
		properties.setRules(new HashMap<>());
		try {
			context.publishEvent(new RefreshScopeRefreshedEvent());
			System.out.println("REAL_REFRESH success");
		}
		catch (Exception e) {
			System.out.println("REAL_REFRESH exception=" + e);
		}
		List<String> failures = new ArrayList<>();
		for (var c : calls.entrySet()) {
			int h = hits.get();
			System.out.println(
					"REAL_FEIGN_PRECALL id=" + c.getKey() + " hasRule=" + DegradeRuleManager.hasConfig(c.getKey()));
			String result = invoke(c.getValue());
			boolean retained = DegradeRuleManager.hasConfig(c.getKey());
			System.out.println("REAL_FEIGN_AFTER id=" + c.getKey() + " hasRule=" + retained + " response=" + result
					+ " backendHitDelta=" + (hits.get() - h));
			if (!result.equals("live") || retained) {
				failures.add(c.getKey());
			}
		}
		assertThat(failures).isEmpty();
	}

	@FeignClient(name = "orders", contextId = "ordersV2", url = "${test.url}")
	interface AliasContext {

		@GetMapping("/")
		String call();

	}

	@FeignClient(value = "otherOrders", contextId = "otherOrdersV2", url = "${test.url}")
	interface ValueContext {

		@GetMapping("/")
		String call();

	}

	@FeignClient(value = "${test.client.name}", url = "${test.url}")
	interface Placeholder {

		@GetMapping("/")
		String call();

	}

	@FeignClient(value = "plain", url = "${test.url}")
	interface Plain {

		@GetMapping("/")
		String call();

	}

	@Configuration
	@EnableAutoConfiguration(
			exclude = com.alibaba.cloud.circuitbreaker.sentinel.ReactiveSentinelCircuitBreakerAutoConfiguration.class)
	@EnableFeignClients(clients = { AliasContext.class, ValueContext.class, Placeholder.class, Plain.class })
	static class App {

	}

}
