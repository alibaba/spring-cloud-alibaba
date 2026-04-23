/*
 * Copyright 2013-present the original author or authors.
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

package com.alibaba.cloud.nacos.client;

import com.alibaba.nacos.api.config.ConfigService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NacosPropertySourceBuilder}.
 */
public class NacosPropertySourceBuilderTest {

	@Test
	void builtPropertySourcePreservesFileExtension() throws Exception {
		ConfigService configService = Mockito.mock(ConfigService.class);
		Mockito.when(configService.getConfig(ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(), ArgumentMatchers.anyLong()))
				.thenReturn("key: value");
		NacosPropertySourceBuilder builder = new NacosPropertySourceBuilder(configService,
				1000L);

		NacosPropertySource source = builder.build("app.yaml", "DEFAULT_GROUP", "yaml",
				true);

		assertThat(source.getFileExtension()).isEqualTo("yaml");
		assertThat(source.getDataId()).isEqualTo("app.yaml");
		assertThat(source.getGroup()).isEqualTo("DEFAULT_GROUP");
		assertThat(source.isRefreshable()).isTrue();
	}

	@Test
	void builtPropertySourcePreservesPropertiesExtension() throws Exception {
		ConfigService configService = Mockito.mock(ConfigService.class);
		Mockito.when(configService.getConfig(ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(), ArgumentMatchers.anyLong()))
				.thenReturn("key=value");
		NacosPropertySourceBuilder builder = new NacosPropertySourceBuilder(configService,
				1000L);

		NacosPropertySource source = builder.build("app", "DEFAULT_GROUP", "properties",
				false);

		assertThat(source.getFileExtension()).isEqualTo("properties");
		assertThat(source.isRefreshable()).isFalse();
	}

}
