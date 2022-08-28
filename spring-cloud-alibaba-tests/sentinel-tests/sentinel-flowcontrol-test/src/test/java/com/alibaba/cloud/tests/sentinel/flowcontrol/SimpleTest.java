/*
 * Copyright 2013-2022 the original author or authors.
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

package com.alibaba.cloud.tests.sentinel.flowcontrol;

import org.junit.jupiter.api.Test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author Freeman
 * @date 2022/8/29
 */
public class SimpleTest {

	@Test
	public void test() {
		RestTemplate rest = new RestTemplate();

		ResponseEntity<String> res = rest
				.getForEntity("http://localhost:" + 8080 + "/flowControl", String.class);

		System.out.println(res.getBody());
	}

}
