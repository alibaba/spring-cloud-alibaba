/*
 * Copyright 2013-2018 the original author or authors.
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

import java.time.Duration;
import java.util.Collections;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Ryan Baxter
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT,
		classes = ReactiveSentinelCircuitBreakerIntegrationTest.Application.class,
		properties = { "spring.cloud.discovery.client.health-indicator.enabled=false" })
@DirtiesContext
public class ReactiveSentinelCircuitBreakerIntegrationTest {

	@LocalServerPort
	private int port = 0;

	@Autowired
	private ReactiveSentinelCircuitBreakerIntegrationTest.Application.DemoControllerService service;

	@Before
	public void setup() {
		service.setPort(port);
	}

	@Test
	public void test() throws Exception {
		StepVerifier.create(service.normal()).expectNext("normal").verifyComplete();
		StepVerifier.create(service.slow()).expectNext("slow").verifyComplete();
		StepVerifier.create(service.slow()).expectNext("slow").verifyComplete();
		StepVerifier.create(service.slow()).expectNext("slow").verifyComplete();
		StepVerifier.create(service.slow()).expectNext("slow").verifyComplete();
		StepVerifier.create(service.slow()).expectNext("slow").verifyComplete();

		// Then in the next 5s, the fallback method should be called.
		for (int i = 0; i < 5; i++) {
			StepVerifier.create(service.slow()).expectNext("fallback").verifyComplete();
			Thread.sleep(1000);
		}

		// Half-open recovery (will re-open the circuit breaker).
		StepVerifier.create(service.slow()).expectNext("slow").verifyComplete();

		StepVerifier.create(service.normalFlux()).expectNext("normalflux")
				.verifyComplete();
		StepVerifier.create(service.slowFlux()).expectNext("slowflux").verifyComplete();
		StepVerifier.create(service.slowFlux()).expectNext("slowflux").verifyComplete();
		StepVerifier.create(service.slowFlux()).expectNext("slowflux").verifyComplete();
		StepVerifier.create(service.slowFlux()).expectNext("slowflux").verifyComplete();
		StepVerifier.create(service.slowFlux()).expectNext("slowflux").verifyComplete();
		// Then in the next 5s, the fallback method should be called.
		for (int i = 0; i < 5; i++) {
			StepVerifier.create(service.slowFlux()).expectNext("flux_fallback")
					.verifyComplete();
			Thread.sleep(1000);
		}

		// Half-open recovery (will re-open the circuit breaker).
		StepVerifier.create(service.slowFlux()).expectNext("slowflux").verifyComplete();
	}

	@Configuration
	@EnableAutoConfiguration
	@RestController
	protected static class Application {

		@GetMapping("/slow")
		public Mono<String> slow() {
			return Mono.just("slow").delayElement(Duration.ofMillis(80));
		}

		@GetMapping("/normal")
		public Mono<String> normal() {
			return Mono.just("normal");
		}

		@GetMapping("/slow_flux")
		public Flux<String> slowFlux() {
			return Flux.just("slow", "flux").delayElements(Duration.ofMillis(80));
		}

		@GetMapping("normal_flux")
		public Flux<String> normalFlux() {
			return Flux.just("normal", "flux");
		}

		@Bean
		public Customizer<ReactiveSentinelCircuitBreakerFactory> slowCustomizer() {
			return factory -> {
				factory.configure(
						builder -> builder.rules(Collections
								.singletonList(new DegradeRule("slow_mono").setCount(50)
										.setSlowRatioThreshold(0.7).setMinRequestAmount(5)
										.setStatIntervalMs(30000).setTimeWindow(5))),
						"slow_mono");
				factory.configure(
						builder -> builder.rules(Collections
								.singletonList(new DegradeRule("slow_mono").setCount(50)
										.setSlowRatioThreshold(0.7).setMinRequestAmount(5)
										.setStatIntervalMs(30000).setTimeWindow(5))),
						"slow_flux");
				factory.configureDefault(id -> new SentinelConfigBuilder()
						.resourceName(id)
						.rules(Collections.singletonList(new DegradeRule(id)
								.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT)
								.setCount(5).setTimeWindow(10)))
						.build());
			};
		}

		@Service
		public static class DemoControllerService {

			private int port = 0;

			private ReactiveCircuitBreakerFactory cbFactory;

			DemoControllerService(ReactiveCircuitBreakerFactory cbFactory) {
				this.cbFactory = cbFactory;
			}

			public Mono<String> slow() {
				return WebClient.builder().baseUrl("http://localhost:" + port).build()
						.get().uri("/slow").retrieve().bodyToMono(String.class)
						.transform(it -> cbFactory.create("slow_mono").run(it, t -> {
							t.printStackTrace();
							return Mono.just("fallback");
						}));
			}

			public Mono<String> normal() {
				return WebClient.builder().baseUrl("http://localhost:" + port).build()
						.get().uri("/normal").retrieve().bodyToMono(String.class)
						.transform(it -> cbFactory.create("normal_mono").run(it, t -> {
							t.printStackTrace();
							return Mono.just("fallback");
						}));
			}

			public Flux<String> slowFlux() {
				return WebClient.builder().baseUrl("http://localhost:" + port).build()
						.get().uri("/slow_flux").retrieve()
						.bodyToFlux(new ParameterizedTypeReference<String>() {
						}).transform(it -> cbFactory.create("slow_flux").run(it, t -> {
							t.printStackTrace();
							return Flux.just("flux_fallback");
						}));
			}

			public Flux<String> normalFlux() {
				return WebClient.builder().baseUrl("http://localhost:" + port).build()
						.get().uri("/normal_flux").retrieve().bodyToFlux(String.class)
						.transform(it -> cbFactory.create("normal_flux").run(it, t -> {
							t.printStackTrace();
							return Flux.just("flux_fallback");
						}));
			}

			public void setPort(int port) {
				this.port = port;
			}

		}

	}

}
