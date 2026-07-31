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

import com.alibaba.cloud.nacos.refresh.NacosSnapshotConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NacosPropertySourceBuilder}.
 *
 * @see <a href="https://github.com/alibaba/spring-cloud-alibaba/issues/4337">#4337</a>
 */
class NacosPropertySourceBuilderTest {

	@Test
	void buildWhenSnapshotIsEmptyThenDoesNotUseRemoteConfig() throws Exception {
		ConfigService configService = Mockito.mock(ConfigService.class);
		Mockito.when(configService.getConfig("builder-test.properties",
				"DEFAULT_GROUP", 3000L)).thenReturn("name=remote");

		NacosSnapshotConfigManager.putConfigSnapshot("builder-test.properties",
				"DEFAULT_GROUP", "");
		try {
			NacosPropertySource propertySource = new NacosPropertySourceBuilder(
					configService, 3000L)
				.build("builder-test.properties", "DEFAULT_GROUP", "properties",
						true);

			assertThat(propertySource.getSource()).isEmpty();
			Mockito.verify(configService, Mockito.never())
				.getConfig("builder-test.properties", "DEFAULT_GROUP", 3000L);
		}
		finally {
			NacosSnapshotConfigManager.removeConfigSnapshot(
					"builder-test.properties", "DEFAULT_GROUP");
		}
	}

	@Test
	void buildPreservesSuffixOnResult() throws Exception {
		ConfigService configService = Mockito.mock(ConfigService.class);
		Mockito.when(configService.getConfig(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyLong())).thenReturn("key: value");
		NacosPropertySourceBuilder builder = new NacosPropertySourceBuilder(configService, 3000L);
		NacosPropertySource result = builder.build("app.yml", "DEFAULT_GROUP", "yml", true);
		assertThat(result.getSuffix()).isEqualTo("yml");
	}

	@Test
	void buildPreservesSuffixAcrossMultipleFormats() throws Exception {
		ConfigService configService = Mockito.mock(ConfigService.class);
		Mockito.when(configService.getConfig(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyLong())).thenReturn(null);
		NacosPropertySourceBuilder builder = new NacosPropertySourceBuilder(configService, 3000L);
		for (String suffix : new String[] { "yml", "yaml", "json", "xml", "properties" }) {
			NacosPropertySource result = builder.build("app", "DEFAULT_GROUP", suffix, true);
			assertThat(result.getSuffix()).isEqualTo(suffix);
		}
	}

}
