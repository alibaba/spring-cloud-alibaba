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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author freeman
 */
@SpringBootApplication
@EnableFeignClients
public class NacosDiscoveryTestApp {

	public static void main(String[] args) {
		SpringApplication.run(NacosDiscoveryTestApp.class, args);
	}

	@Bean
	@LoadBalanced
	@Profile("service-1")
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@RestController
	@Profile("service-2")
	static class Controller {
		int port;

		@GetMapping
		public Object get() {
			return port;
		}

		@GetMapping("/{ok}")
		public Object pass(@PathVariable Boolean ok) {
			if (ok) {
				return "ok";
			}
			throw new RuntimeException("not ok!");
		}

		@EventListener
		public void onApplicationEvent(ServletWebServerInitializedEvent event) {
			port = event.getWebServer().getPort();
		}
	}

	@FeignClient(value = "service-2", fallback = Fallback.class)
	interface Service2Client {
		@GetMapping
		Object get();

		@GetMapping("/{ok}")
		String pass(@PathVariable Boolean ok);
	}

	@Component
	static class Fallback implements Service2Client {
		@Override
		public Object get() {
			return "shouldn't use this !";
		}

		@Override
		public String pass(Boolean ok) {
			return "fallback";
		}
	}

}
