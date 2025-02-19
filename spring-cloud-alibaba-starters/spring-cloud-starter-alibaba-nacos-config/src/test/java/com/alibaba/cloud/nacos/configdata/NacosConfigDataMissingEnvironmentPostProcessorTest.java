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

package com.alibaba.cloud.nacos.configdata;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

/**
 *
 * @author Ryan Baxter
 * @author freeman
 */
class NacosConfigDataMissingEnvironmentPostProcessorTest {

	@Test
	void noSpringConfigImport() {
		MockEnvironment environment = new MockEnvironment();
		SpringApplication app = Mockito.mock(SpringApplication.class);
		NacosConfigDataMissingEnvironmentPostProcessor processor = new NacosConfigDataMissingEnvironmentPostProcessor();
		Assertions.assertThatThrownBy(() -> processor.postProcessEnvironment(environment, app))
				.isInstanceOf(
						NacosConfigDataMissingEnvironmentPostProcessor.ImportException.class);
	}

	@Test
	void boostrap() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.cloud.bootstrap.enabled", "true");
		SpringApplication app = Mockito.mock(SpringApplication.class);
		NacosConfigDataMissingEnvironmentPostProcessor processor = new NacosConfigDataMissingEnvironmentPostProcessor();
		Assertions.assertThatCode(() -> processor.postProcessEnvironment(environment, app))
				.doesNotThrowAnyException();
	}

	@Test
	void legacy() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.config.use-legacy-processing", "true");
		SpringApplication app = Mockito.mock(SpringApplication.class);
		NacosConfigDataMissingEnvironmentPostProcessor processor = new NacosConfigDataMissingEnvironmentPostProcessor();
		Assertions.assertThatCode(() -> processor.postProcessEnvironment(environment, app))
				.doesNotThrowAnyException();
	}

	@Test
	void configNotEnabled() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.cloud.nacos.config.enabled", "false");
		SpringApplication app = Mockito.mock(SpringApplication.class);
		NacosConfigDataMissingEnvironmentPostProcessor processor = new NacosConfigDataMissingEnvironmentPostProcessor();
		Assertions.assertThatCode(() -> processor.postProcessEnvironment(environment, app))
				.doesNotThrowAnyException();
	}

	@Test
	void importCheckNotEnabled() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.cloud.nacos.config.import-check.enabled",
				"false");
		SpringApplication app = Mockito.mock(SpringApplication.class);
		NacosConfigDataMissingEnvironmentPostProcessor processor = new NacosConfigDataMissingEnvironmentPostProcessor();
		Assertions.assertThatCode(() -> processor.postProcessEnvironment(environment, app))
				.doesNotThrowAnyException();
	}

	@Test
	void importSinglePropertySource() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.config.import", "nacos:test.yml");
		SpringApplication app = Mockito.mock(SpringApplication.class);
		NacosConfigDataMissingEnvironmentPostProcessor processor = new NacosConfigDataMissingEnvironmentPostProcessor();
		Assertions.assertThatCode(() -> processor.postProcessEnvironment(environment, app))
				.doesNotThrowAnyException();
	}

	@Test
	void importMultiplePropertySource() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.config.import", "nacos:test.yml");
		SpringApplication app = Mockito.mock(SpringApplication.class);
		NacosConfigDataMissingEnvironmentPostProcessor processor = new NacosConfigDataMissingEnvironmentPostProcessor();
		Assertions.assertThatCode(() -> processor.postProcessEnvironment(environment, app))
				.doesNotThrowAnyException();
	}

	@Test
	void importMultiplePropertySourceAsList() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.config.import[0]", "nacos:test.yml");
		environment.setProperty("spring.config.import[1]", "file:./app.properties");
		SpringApplication app = Mockito.mock(SpringApplication.class);
		NacosConfigDataMissingEnvironmentPostProcessor processor = new NacosConfigDataMissingEnvironmentPostProcessor();
		Assertions.assertThatCode(() -> processor.postProcessEnvironment(environment, app))
				.doesNotThrowAnyException();
	}

}
