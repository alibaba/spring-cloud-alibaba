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

package com.alibaba.cloud.nacos;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * NacosConfigProperties Tester.
 */
public class NacosConfigPropertiesTest {

	@Test
	void testSensitivePropertiesMaskedInToString() {
		NacosConfigProperties properties = new NacosConfigProperties();
		properties.setEndpoint("endpoint-test");
		properties.setAccessKey("ak-test-123");
		properties.setPassword("pwd-test-789");
		properties.setSecretKey("sk-test-456");
		properties.setServerAddr("127.0.0.1:8848");
		String text = properties.toString();

		Assertions.assertThat(text).contains("accessKey='******'");
		Assertions.assertThat(text).contains("secretKey='******'");
		Assertions.assertThat(text).doesNotContain("ak-test-123");
		Assertions.assertThat(text).doesNotContain("sk-test-456");
		Assertions.assertThat(text).contains("endpoint='endpoint-test'");
		Assertions.assertThat(text).contains("serverAddr='127.0.0.1:8848'");
	}
}
