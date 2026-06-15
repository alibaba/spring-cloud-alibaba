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

package com.alibaba.cloud.nacos.configdata;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.cloud.nacos.configdata.NacosConfigDataResource.NacosItemConfig;
import com.alibaba.cloud.nacos.refresh.NacosSnapshotConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import org.junit.jupiter.api.Test;

import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.DefaultBootstrapContext;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NacosConfigDataLoaderTest {

	@Test
	void loadWhenSnapshotExistsThenUsesSnapshotBeforeRemoteConfig() throws Exception {
		ConfigService configService = mock(ConfigService.class);
		when(configService.getConfig("test.properties", "DEFAULT_GROUP", 3000L))
			.thenReturn("name=remote");

		NacosSnapshotConfigManager.putConfigSnapshot("test.properties", "DEFAULT_GROUP",
				"name=snapshot");
		try {
			ConfigData configData = load(configService);

			assertThat(configData).isNotNull();
			assertThat(configData.getPropertySources()).hasSize(1);
			assertThat(configData.getPropertySources().get(0))
				.isInstanceOf(NacosPropertySource.class);
			assertThat(configData.getPropertySources().get(0).getName())
				.isEqualTo("test.properties,DEFAULT_GROUP");
			assertThat(configData.getPropertySources().get(0).getProperty("name"))
				.isEqualTo("snapshot");
			verify(configService, never()).getConfig("test.properties", "DEFAULT_GROUP",
					3000L);
		}
		finally {
			NacosSnapshotConfigManager.removeConfigSnapshot("test.properties",
					"DEFAULT_GROUP");
		}
	}

	@Test
	void loadWhenSnapshotIsEmptyThenDoesNotUseRemoteConfig() throws Exception {
		ConfigService configService = mock(ConfigService.class);
		when(configService.getConfig("test.properties", "DEFAULT_GROUP", 3000L))
			.thenReturn("name=remote");

		NacosSnapshotConfigManager.putConfigSnapshot("test.properties", "DEFAULT_GROUP",
				"");
		try {
			ConfigData configData = load(configService);

			assertThat(configData).isNotNull();
			assertThat(configData.getPropertySources()).hasSize(1);
			assertThat(configData.getPropertySources().get(0))
				.isInstanceOf(NacosPropertySource.class);
			assertThat(((NacosPropertySource) configData.getPropertySources().get(0))
				.getSource()).isEmpty();
			verify(configService, never()).getConfig("test.properties", "DEFAULT_GROUP",
					3000L);
		}
		finally {
			NacosSnapshotConfigManager.removeConfigSnapshot("test.properties",
					"DEFAULT_GROUP");
		}
	}

	private ConfigData load(ConfigService configService) {
		NacosConfigManager configManager = mock(NacosConfigManager.class);
		when(configManager.getConfigService()).thenReturn(configService);

		NacosConfigProperties properties = new NacosConfigProperties();
		properties.setTimeout(3000);

		DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext();
		bootstrapContext.register(Binder.class,
				BootstrapRegistry.InstanceSupplier.of(Binder.get(new MockEnvironment())));
		bootstrapContext.register(NacosConfigManager.class,
				BootstrapRegistry.InstanceSupplier.of(configManager));
		bootstrapContext.register(NacosConfigProperties.class,
				BootstrapRegistry.InstanceSupplier.of(properties));

		ConfigDataLoaderContext context = () -> bootstrapContext;
		NacosConfigDataResource resource = new NacosConfigDataResource(properties, false,
				mock(Profiles.class), new DeferredLogs().getLog(getClass()),
				new NacosItemConfig("DEFAULT_GROUP", "test.properties", "properties", true, ""));

		return new NacosConfigDataLoader(new DeferredLogs()).load(context, resource);
	}

}
