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

package com.alibaba.cloud.examples.kubernetes.config.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static com.alibaba.cloud.examples.kubernetes.config.filter.BlacklistFilter.HEADER_USER_ID;

/**
 * @author Freeman
 */
@RestController
public class EchoController {

	@GetMapping("/echo")
	public String echo(@RequestHeader(HEADER_USER_ID) String userId) {
		return String.format("Hello, %s", userId);
	}

}
