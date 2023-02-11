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

import com.alibaba.cloud.examples.configuration.HttpbinClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author raozihao
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
@RestController
public class TestController {

	@Autowired
	private HttpbinClient httpbinClient;

	@GetMapping("/rt")
	public String delay() {
		return httpbinClient.delay();
	}

	@GetMapping("/exp")
	public String exp() {
		return httpbinClient.status500();
	}

	@GetMapping("/get")
	public String get() {
		return httpbinClient.get();
	}

}
