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

package com.alibaba.cloud.examples;


import com.alibaba.cloud.examples.feign.EchoClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


/**
 * Example of remote invocation of service fusing and load balancing.
 *
 * @author xiaojing, fangjian0423, MieAh
 */
@RestController
public class TestController {

	@Autowired
	private RestTemplate urlCleanedRestTemplate;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private EchoClient echoClient;

	@Autowired
	private DiscoveryClient discoveryClient;

	private static final String SERVICE_PROVIDER_ADDRESS = "http://service-provider";

	@GetMapping("/echo-rest/{str}")
	public String rest(@PathVariable String str) {
		return urlCleanedRestTemplate
				.getForObject(SERVICE_PROVIDER_ADDRESS + "/echo/" + str,
						String.class);
	}

	@GetMapping("/index")
	public String index() {
		return restTemplate.getForObject(SERVICE_PROVIDER_ADDRESS, String.class);
	}

	@GetMapping("/test")
	public String test() {
		return restTemplate
				.getForObject(SERVICE_PROVIDER_ADDRESS + "/test", String.class);
	}

	@GetMapping("/sleep")
	public String sleep() {
		return restTemplate
				.getForObject(SERVICE_PROVIDER_ADDRESS + "/sleep", String.class);
	}

	@GetMapping("/notFound-feign")
	public String notFound() {
		return echoClient.notFound();
	}

	@GetMapping("/divide-feign")
	public String divide(@RequestParam Integer a, @RequestParam Integer b) {
		return echoClient.divide(a, b);
	}

	@GetMapping("/divide-feign2")
	public String divide(@RequestParam Integer a) {
		return echoClient.divide(a);
	}

	@GetMapping("/echo-feign/{str}")
	public String feign(@PathVariable String str) {
		return echoClient.echo(str);
	}

	@GetMapping("/services/{service}")
	public Object client(@PathVariable String service) {
		return discoveryClient.getInstances(service);
	}

	@GetMapping("/services")
	public Object services() {
		return discoveryClient.getServices();
	}

}
