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

import com.alibaba.cloud.examples.feign.OrderClient;
import com.alibaba.cloud.examples.feign.UserClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author freeman
 */
@RestController
public class TestController {

	@Autowired
	private UserClient userClient;
	@Autowired
	private OrderClient orderClient;

	@GetMapping("/test/default/{ok}")
	public String testDefault(@PathVariable boolean ok) {
		return orderClient.defaultConfig(ok);
	}

	@GetMapping("/test/feign/{ok}")
	public String testFeign(@PathVariable boolean ok) {
		return userClient.feign(ok);
	}

	@GetMapping("/test/feignMethod/{ok}")
	public String testFeignMethod(@PathVariable boolean ok) {
		return userClient.feignMethod(ok);
	}

}
