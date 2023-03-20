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

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.springframework.test.annotation.Repeat;
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
		properties = {"spring.cloud.discovery.client.health-indicator.enabled=false"})
@DirtiesContext
public class ReactiveSentinelCircuitBreakerIntegrationTest {

	@LocalServerPort
	private int port = 0;

	@Autowired
	private ReactiveSentinelCircuitBreakerIntegrationTest.Application.DemoControllerService service;

	private final AtomicInteger idx = new AtomicInteger(0);

	@Before
	public void setup() {
		service.setPort(port);
	}

	@Repeat(100)
	@Test
	public void test() throws Exception {
		int idx = this.idx.getAndIncrement();
		StepVerifier.create(service.slowFlux(idx)).expectNext("slowflux").verifyComplete();
		StepVerifier.create(service.slowFlux(idx)).expectNext("slowflux").verifyComplete();
		StepVerifier.create(service.slowFlux(idx)).expectNext("slowflux").verifyComplete();
		StepVerifier.create(service.slowFlux(idx)).expectNext("slowflux").verifyComplete();
		StepVerifier.create(service.slowFlux(idx)).expectNext("slowflux").verifyComplete();
		StepVerifier.create(service.slowFlux(idx))
				.expectNextMatches(ret -> {
					if ("slowflux".equals(ret)) {
						System.out.println("======slowflux======");
					}
					return "slowflux".equals(ret) || "flux_fallback".equals(ret);
				}).verifyComplete();

		StepVerifier.create(service.slowFlux(idx)).expectNext("flux_fallback")
				.verifyComplete();
	}

	@Configuration
	@EnableAutoConfiguration
	@RestController
	protected static class Application {

		@GetMapping("/slow_flux")
		public Flux<String> slowFlux() {
			return Flux.just("slow", "flux").delayElements(Duration.ofMillis(80));
		}

		@Bean
		public Customizer<ReactiveSentinelCircuitBreakerFactory> slowCustomizer() {
			return factory -> {
				for (int i = 0; i < 100; i++) {
					final int finalI = i;
					factory.configure(
							builder -> builder.rules(Collections
									.singletonList(new DegradeRule("slow_flux" + finalI).setCount(50)
											.setSlowRatioThreshold(0.7).setMinRequestAmount(5)
											.setStatIntervalMs(30000).setTimeWindow(5))),
							"slow_flux" + finalI);
				}
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

			public Flux<String> slowFlux(int idx) {
				return WebClient.builder().baseUrl("http://localhost:" + port).build()
						.get().uri("/slow_flux").retrieve()
						.bodyToFlux(new ParameterizedTypeReference<String>() {
						}).transform(it -> cbFactory.create("slow_flux" + idx).run(it, t -> {
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
