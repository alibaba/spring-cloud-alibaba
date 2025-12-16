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

package com.alibaba.cloud.tests.nacos.discovery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.alibaba.cloud.testsupport.ContainerStarter;
import com.alibaba.cloud.testsupport.HasDockerAndItEnabled;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.alibaba.cloud.tests.nacos.discovery.NacosDiscoveryTestApp.Service2Client;
import static com.alibaba.cloud.testsupport.Tester.justDo;
import static com.alibaba.cloud.testsupport.Tester.testFunction;
import static java.util.function.Predicate.isEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 *
 *
 * @author freeman
 */
@HasDockerAndItEnabled
public class NacosDiscoveryTest {

	static GenericContainer nacos;
	static ConfigurableApplicationContext service1 = new SpringApplicationBuilder(
			NacosDiscoveryTestApp.class).profiles("service-1").run();
	static ConfigurableApplicationContext service2_0 = new SpringApplicationBuilder(
			NacosDiscoveryTestApp.class).profiles("service-2").run();
	static ConfigurableApplicationContext service2_1 = new SpringApplicationBuilder(
			NacosDiscoveryTestApp.class).profiles("service-2").run();

	static {
		nacos = ContainerStarter.startNacos();

		// wait nacos started
		justDo(() -> Thread.sleep(4000L));
	}

	static {
		String serverAddr = "localhost:" + nacos.getMappedPort(8848);
		System.setProperty("spring.cloud.nacos.discovery.server-addr", serverAddr);
	}

	@BeforeAll
	static void init() {
		// give nacos a break
		justDo(() -> Thread.sleep(1000L));
	}

	@AfterAll
	static void stop() {
		service1.stop();
		service2_0.stop();
		service2_1.stop();
	}

	@Test
	public void testServiceDiscovery() {
		testFunction("Service discovery", () -> {
			DiscoveryClient discoveryClient = service1.getBean(DiscoveryClient.class);
			List<ServiceInstance> instances = discoveryClient.getInstances("service-2");
			assertThat(instances).hasSize(2);
		});
	}

	@Test
	public void testServiceDiscoveryWithRestTemplate() {
		testFunction("Service discovery with RestTemplate", () -> {
			RestTemplate restTemplate = service1.getBean(RestTemplate.class);
			assertThatThrownBy(() -> service2_0.getBean(RestTemplate.class));

			// default using RR
			List<Object> list1 = new ArrayList<>();
			List<Object> list2 = new ArrayList<>();
			for (int i = 0; i < 20; i++) {
				ResponseEntity<String> response = restTemplate
						.getForEntity("http://service-2", String.class);
				if (i % 2 != 0) {
					list1.add(response.getBody());
				}
				else {
					list2.add(response.getBody());
				}
			}

			assertThat(list1).hasSize(10);
			assertThat(list1.stream().allMatch(isEqual(list1.get(0)))).isTrue();
			assertThat(list2).hasSize(10);
			assertThat(list2.stream().allMatch(isEqual(list2.get(0)))).isTrue();
		});
	}

	@Test
	public void testServiceDiscoveryWithFeignClient() {
		testFunction("Service discovery with FeignClient", () -> {
			Service2Client service2Client = service1.getBean(Service2Client.class);

			// default using RR
			List<Object> list1 = new ArrayList<>();
			List<Object> list2 = new ArrayList<>();
			for (int i = 0; i < 20; i++) {
				Object result = service2Client.get();
				if (i % 2 != 0) {
					list1.add(result);
				}
				else {
					list2.add(result);
				}
			}

			assertThat(list1).hasSize(10);
			assertThat(list1.stream().allMatch(isEqual(list1.get(0)))).isTrue();
			assertThat(list2).hasSize(10);
			assertThat(list2.stream().allMatch(isEqual(list2.get(0)))).isTrue();
		});
	}

	@Test
	public void testServiceDiscoveryWithFeignClientUsingSentinelCircuitBreaker() {
		testFunction("Service discovery with FeignClient using Sentinel Circuit Breaker",
				() -> {
					Service2Client service2Client = service1
							.getBean(Service2Client.class);

					List<Object> passResult = new ArrayList<>();
					List<Object> fallbackResult = new ArrayList<>();
					for (int i = 0; i < 10; i++) {
						passResult.add(service2Client.pass(true));
						fallbackResult.add(service2Client.pass(false));
					}

					assertThat(passResult.stream().allMatch(isEqual("ok"))).isTrue();
					assertThat(fallbackResult.stream().allMatch(isEqual("fallback")))
							.isTrue();
				});
	}

	@Test
	public void testServiceDiscoveryActuatorEndpoint() {
		testFunction("Service discovery actuator endpoint", () -> {

			WebServerApplicationContext webServerApplicationContext = (WebServerApplicationContext) service1;
			int port = webServerApplicationContext.getWebServer().getPort();

			String response = new RestTemplate().getForEntity(
					String.format("http://127.0.0.1:%d/actuator/nacosdiscovery", port),
					String.class).getBody();

			LinkedHashMap map = new ObjectMapper().readValue(response,
					LinkedHashMap.class);

			assertThat(map.containsKey("subscribe")).isTrue();
			assertThat(map.containsKey("NacosDiscoveryProperties")).isTrue();
		});
	}

}
