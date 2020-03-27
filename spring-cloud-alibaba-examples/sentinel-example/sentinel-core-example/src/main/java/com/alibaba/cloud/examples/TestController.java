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

package com.alibaba.cloud.examples;

import com.alibaba.csp.sentinel.annotation.SentinelResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author xiaojing
 */
@RestController
public class TestController {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private CircuitBreakerFactory circuitBreakerFactory;

	@GetMapping("/hello")
	@SentinelResource("resource")
	public String hello() {
		return "Hello";
	}

	@GetMapping("/aa")
	@SentinelResource("aa")
	public String aa(int b, int a) {
		return "Hello test";
	}

	@GetMapping("/test")
	public String test1() {
		return "Hello test";
	}

	@GetMapping("/template")
	public String client() {
		return restTemplate.getForObject("http://www.taobao.com/test", String.class);
	}

	@GetMapping("/slow")
	public String slow() {
		return circuitBreakerFactory.create("slow").run(() -> {
			try {
				Thread.sleep(500L);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			return "slow";
		}, throwable -> "fallback");
	}

}
