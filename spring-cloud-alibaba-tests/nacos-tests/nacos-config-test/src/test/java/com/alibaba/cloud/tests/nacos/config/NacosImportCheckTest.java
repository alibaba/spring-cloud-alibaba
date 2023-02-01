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

package com.alibaba.cloud.tests.nacos.config;

import com.alibaba.cloud.testsupport.HasDockerAndItEnabled;
import org.junit.jupiter.api.Test;

import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.cloud.commons.ConfigDataMissingEnvironmentPostProcessor.ImportException;

/**
 *
 * @author freeman
 */
@HasDockerAndItEnabled
public class NacosImportCheckTest {

	@Test
	public void testImportCheckEnabled() {
		assertThatExceptionOfType(ImportException.class).isThrownBy(
				() -> new SpringApplicationBuilder(NacosConfigTestApplication.class)
						.properties("server.port=0")
						.properties("spring.application.name=import-check-enabled")
						.run());
	}

	@Test
	public void testImportCheckDisabled() {
		assertThatCode(
				() -> new SpringApplicationBuilder(NacosConfigTestApplication.class)
						.properties("server.port=0")
						.properties("spring.application.name=import-check-disabled")
						.properties(
								"spring.cloud.nacos.config.import-check.enabled=false")
						.run()).doesNotThrowAnyException();
	}

}
