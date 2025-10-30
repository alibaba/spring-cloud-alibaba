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

package com.alibaba.cloud.examples.controller;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * Endpoints to demonstrate RestClient + Sentinel integration.
 */
@RestController
public class TestController {

	private final RestClient restClient;

	public TestController(RestClient restClient) {
		this.restClient = restClient;
	}


	@GetMapping("/get")
	public ResponseEntity<String> get() {
		return restClient.get()
				.uri("https://httpbin.org/get")
				.exchange((req, res) -> {
					String body = StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8);
					HttpHeaders headers = new HttpHeaders();
					headers.putAll(res.getHeaders());
					// 透传真实状态码 (200 / 429)
					return ResponseEntity
							.status(HttpStatus.valueOf(res.getStatusCode().value()))
							.headers(headers)
							.body(body);
				});
	}


	@GetMapping("/status/500")
	public ResponseEntity<String> status500() {
		return restClient.get()
				.uri("https://httpbin.org/status/500")
				.exchange((req, res) -> {
					String body = StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8);
					return ResponseEntity
							.status(HttpStatus.valueOf(res.getStatusCode().value()))
							.body(body);
				});
	}


	@GetMapping("/delay/3")
	public ResponseEntity<String> delay3() {
		return restClient.get()
				.uri("https://httpbin.org/delay/3")
				.exchange((req, res) -> {
					String body = StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8);
					return ResponseEntity
							.status(HttpStatus.valueOf(res.getStatusCode().value()))
							.body(body);
				});
	}
}
