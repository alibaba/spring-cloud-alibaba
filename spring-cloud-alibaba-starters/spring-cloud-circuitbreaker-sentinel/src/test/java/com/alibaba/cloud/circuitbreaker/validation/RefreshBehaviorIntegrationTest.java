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

package com.alibaba.cloud.circuitbreaker.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.cloud.circuitbreaker.sentinel.SentinelCircuitBreakerFactory;
import com.alibaba.cloud.circuitbreaker.sentinel.feign.CircuitBreakerRuleChangeListener;
import com.alibaba.cloud.circuitbreaker.sentinel.feign.SentinelFeignClientProperties;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshBehaviorIntegrationTest {

	SentinelFeignClientProperties p;

	SentinelCircuitBreakerFactory f;

	GenericApplicationContext ctx;

	static DegradeRule rule() {
		return new DegradeRule().setGrade(2)
			.setCount(1)
			.setMinRequestAmount(1)
			.setTimeWindow(120)
			.setStatIntervalMs(300000);
	}

	@BeforeEach
	void setup() {
		DegradeRuleManager.loadRules(new ArrayList<>());
		p = new SentinelFeignClientProperties();
		f = new SentinelCircuitBreakerFactory();
		ctx = new GenericApplicationContext();
	}

	@AfterEach
	void cleanup() {
		ctx.close();
		DegradeRuleManager.loadRules(new ArrayList<>());
	}

	void start() {
		CircuitBreakerRuleChangeListener.configureDefault(p, f);
		CircuitBreakerRuleChangeListener.configureCustom(p, f);
		ctx.registerBean("properties", SentinelFeignClientProperties.class, () -> p);
		ctx.registerBean("factory", SentinelCircuitBreakerFactory.class, () -> f);
		ctx.registerBean("listener", CircuitBreakerRuleChangeListener.class);
		ctx.refresh();
	}

	void refresh() {
		ctx.publishEvent(new RefreshScopeRefreshedEvent());
	}

	void open(CircuitBreaker cb) {
		for (int i = 0; i < 3; i++) {
			cb.run(() -> {
				throw new IllegalStateException("backend failure");
			}, e -> "fallback");
		}
		assertThat(cb.run(() -> "live", e -> "blocked")).isEqualTo("blocked");
	}

	String request(String resource) {
		return f.create(resource).run(() -> "live", e -> "blocked");
	}

	@Test
	void customRemovalRestoresRealCallsAndPreservesOtherResource() {
		p.getRules().put("custom", List.of(rule()));
		start();
		open(f.create("custom"));
		DegradeRule other = rule();
		other.setResource("unrelated");
		DegradeRuleManager.setRulesForResource("unrelated", Set.of(other));
		p.setRules(new HashMap<>());
		refresh();
		assertThat(DegradeRuleManager.hasConfig("custom")).isFalse();
		assertThat(request("custom")).isEqualTo("live");
		assertThat(DegradeRuleManager.hasConfig("unrelated")).isTrue();
		refresh();
		assertThat(request("custom")).isEqualTo("live");
	}

	@Test
	void scannedValueDefaultRemovalRestoresRealCalls() {
		ctx.registerBean(ValueClient.class.getName(), ValueClient.class,
				() -> (ValueClient) java.lang.reflect.Proxy.newProxyInstance(ValueClient.class.getClassLoader(),
						new Class[] { ValueClient.class }, (o, m, a) -> null));
		p.getRules().put(p.getDefaultRule(), List.of(rule()));
		start();
		open(f.create("scanned"));
		p.setRules(new HashMap<>());
		refresh();
		assertThat(request("scanned")).isEqualTo("live");
		assertThat(DegradeRuleManager.hasConfig("scanned")).isFalse();
	}

	@Test
	void scannedNameAliasDefaultRemovalRestoresRealCalls() {
		ctx.registerBean(NameClient.class.getName(), NameClient.class, () -> (NameClient) java.lang.reflect.Proxy
			.newProxyInstance(NameClient.class.getClassLoader(), new Class[] { NameClient.class }, (o, m, a) -> null));
		p.getRules().put(p.getDefaultRule(), List.of(rule()));
		start();
		open(f.create("aliased"));
		p.setRules(new HashMap<>());
		refresh();
		assertThat(request("aliased")).isEqualTo("live");
		assertThat(DegradeRuleManager.hasConfig("aliased")).isFalse();
	}

	@Test
	void changedRuleReplacesOldAndResetsCircuit() {
		p.getRules().put("custom", List.of(rule()));
		start();
		open(f.create("custom"));
		p.setRules(new HashMap<>(Map.of("custom", List.of(rule().setCount(100)))));
		refresh();
		assertThat(request("custom")).isEqualTo("live");
		assertThat(DegradeRuleManager.getRulesOfResource("custom")).hasSize(1);
		assertThat(DegradeRuleManager.getRulesOfResource("custom").iterator().next().getCount()).isEqualTo(100);
	}

	@Test
	void removalFallsBackToDefault() {
		p.getRules().put("custom", List.of(rule()));
		p.getRules().put(p.getDefaultRule(), List.of(rule().setCount(100)));
		start();
		open(f.create("custom"));
		p.getRules().remove("custom");
		refresh();
		assertThat(request("custom")).isEqualTo("live");
		assertThat(DegradeRuleManager.getRulesOfResource("custom")).hasSize(1);
		assertThat(DegradeRuleManager.getRulesOfResource("custom").iterator().next().getCount()).isEqualTo(100);
	}

	@Test
	void httpBackendCallResumesAfterRemoval() throws Exception {
		AtomicInteger hits = new AtomicInteger();
		AtomicInteger status = new AtomicInteger(500);
		com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer
			.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/backend", ex -> {
			hits.incrementAndGet();
			byte[] body = "backend".getBytes();
			ex.sendResponseHeaders(status.get(), body.length);
			ex.getResponseBody().write(body);
			ex.close();
		});
		server.start();
		try {
			java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
			java.net.http.HttpRequest req = java.net.http.HttpRequest
				.newBuilder(java.net.URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/backend"))
				.build();
			java.util.function.Supplier<String> call = () -> {
				try {
					int code = client.send(req, java.net.http.HttpResponse.BodyHandlers.discarding()).statusCode();
					if (code != 200) {
						throw new IllegalStateException("HTTP " + code);
					}
					return "HTTP 200";
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
			p.getRules().put("http-resource", List.of(rule()));
			start();
			CircuitBreaker cb = f.create("http-resource");
			for (int i = 0; i < 3; i++) {
				cb.run(call, e -> "fallback");
			}
			status.set(200);
			int before = hits.get();
			assertThat(cb.run(call, e -> "blocked")).isEqualTo("blocked");
			assertThat(hits.get()).isEqualTo(before);
			p.setRules(new HashMap<>());
			refresh();
			assertThat(f.create("http-resource").run(call, e -> "blocked")).isEqualTo("HTTP 200");
			assertThat(hits.get()).isEqualTo(before + 1);
			System.out.println("HTTP_EVIDENCE before_refresh_backend_hits=" + before + " after_refresh_backend_hits="
					+ hits.get() + " response=200");
		}
		finally {
			server.stop(0);
		}
	}

	@Test
	void unchangedRefreshKeepsOpenCircuit() {
		DegradeRule same = rule();
		same.setResource("unchanged");
		p.getRules().put("unchanged", List.of(same));
		start();
		open(f.create("unchanged"));
		refresh();
		assertThat(request("unchanged")).isEqualTo("blocked");
	}

	@Test
	void emptyFactoryConfigurationDoesNotOwnExternalRule() {
		start();
		f.create("external");
		DegradeRule external = rule();
		external.setResource("external");
		DegradeRuleManager.setRulesForResource("external", Set.of(external));
		p.getRules().put("other", List.of(rule()));
		refresh();
		assertThat(DegradeRuleManager.hasConfig("external")).isTrue();
	}

	@FeignClient("scanned")
	interface ValueClient {

	}

	@FeignClient(name = "aliased")
	interface NameClient {

	}

}
