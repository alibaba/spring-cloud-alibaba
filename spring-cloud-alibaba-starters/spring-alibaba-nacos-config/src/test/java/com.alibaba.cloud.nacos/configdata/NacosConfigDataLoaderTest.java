/*
 * Copyright 2013-2025 the original author or authors.
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
import com.alibaba.cloud.nacos.configdata.NacosConfigDataResource.NacosItemConfig;
import com.alibaba.nacos.api.config.ConfigService;
import org.junit.jupiter.api.Test;

import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.DefaultBootstrapContext;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataException;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NacosConfigDataLoaderTest {

	@Test
	void loadWhenContentUnparseableThenThrowsParseExceptionInsteadOfNotFound() throws Exception {
		ConfigService configService = mock(ConfigService.class);
		// duplicate YAML key: the content is fetched fine, but SnakeYAML rejects it
		// while parsing, which must not be reported as "config resource does not exist".
		when(configService.getConfig("test.yaml", "DEFAULT_GROUP", 3000L)).thenReturn(
				"single:\n  oss:\n    path-style-access: true\n"
						+ "single:\n  oss:\n    path-style-access: true\n");

		NacosConfigDataLoader loader = new NacosConfigDataLoader(new DeferredLogs());

		assertThatThrownBy(
				() -> loader.load(context(configService), resource("test.yaml", "yaml", false)))
			.isInstanceOf(NacosConfigParseException.class)
			.isInstanceOf(ConfigDataException.class)
			.isNotInstanceOf(ConfigDataResourceNotFoundException.class)
			.hasMessageContaining("Failed to parse")
			.hasMessageContaining("test.yaml")
			.hasCauseInstanceOf(Exception.class);
	}

	private ConfigData load(ConfigService configService) {
		return new NacosConfigDataLoader(new DeferredLogs()).load(context(configService),
				resource("test.properties", "properties", false));
	}

	private ConfigDataLoaderContext context(ConfigService configService) {
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

		return () -> bootstrapContext;
	}

	private NacosConfigDataResource resource(String dataId, String suffix,
			boolean optional) {
		NacosConfigProperties properties = new NacosConfigProperties();
		return new NacosConfigDataResource(properties, optional, mock(Profiles.class),
				new DeferredLogs().getLog(getClass()),
				new NacosItemConfig("DEFAULT_GROUP", dataId, suffix, true, ""));
	}

}
