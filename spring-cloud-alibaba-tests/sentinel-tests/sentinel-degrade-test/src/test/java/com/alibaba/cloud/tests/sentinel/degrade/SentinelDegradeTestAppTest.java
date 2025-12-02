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

import java.net.URI;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.client.RestTestClient;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class SentinelDegradeTestAppTest {

	@LocalServerPort
	int port;

	@Bean
	public RestTestClient restTestClient() {
		// 使用动态注入的端口来构建基础URL
		String baseUrl = "http://localhost:" + port;
		return RestTestClient.bindToServer().baseUrl(baseUrl).build();
	}

	@Test
	public void testDegradeRule() {
		String responseBody = restTestClient().get().uri(URI.create("/degrade")).exchange().expectBody(String.class).returnResult().getResponseBody();
		assertThat(responseBody).contains("fallback");
	}

}
