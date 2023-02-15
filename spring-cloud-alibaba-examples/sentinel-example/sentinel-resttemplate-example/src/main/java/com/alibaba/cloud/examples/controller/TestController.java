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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author raozihao
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
@RestController
public class TestController {

	@Autowired
	RestTemplate restTemplate;

	@GetMapping("/exp")
	public String exp() {
		return restTemplate.getForObject("https://httpbin.org/status/500", String.class);
	}

	@GetMapping("/rt")
	public String rt() {
		return restTemplate.getForObject("https://httpbin.org/delay/3", String.class);
	}

	@GetMapping("/get")
	public String get() {
		return restTemplate.getForObject("https://httpbin.org/get", String.class);
	}

}
