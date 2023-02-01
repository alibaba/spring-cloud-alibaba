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

package com.alibaba.cloud.tests.sentinel.degrade;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static com.alibaba.cloud.tests.sentinel.degrade.Util.FLOW_CONTROL_NOT_TRIGGERED;
import static com.alibaba.cloud.tests.sentinel.degrade.Util.FLOW_CONTROL_TRIGGERED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class SentinelFlowControlTestAppTest {

	@LocalServerPort
	int port;

	@Autowired
	TestRestTemplate rest;

	@Test
	void testFlowControl_whenNotTriggered() {
		final int count = 3;
		List<String> result = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			ResponseEntity<String> res = rest.getForEntity(
					"http://localhost:" + port + FLOW_CONTROL_NOT_TRIGGERED,
					String.class);
			result.add(res.getBody());
		}

		assertThat(result).doesNotContain("fallback");
	}

	@Test
	void testFlowControl_whenTriggered() {
		final int count = 3;
		List<String> result = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			ResponseEntity<String> res = rest.getForEntity(
					"http://localhost:" + port + FLOW_CONTROL_TRIGGERED, String.class);
			result.add(res.getBody());
		}

		assertThat(result).containsSequence("fallback");
	}

}
