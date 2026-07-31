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

package com.alibaba.cloud.nacos.refresh;

import java.util.Collections;
import java.util.Date;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.nacos.api.config.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for suffix-aware refresh in {@link NacosPropertySourceRefreshListener}.
 *
 * @see <a href="https://github.com/alibaba/spring-cloud-alibaba/issues/4337">#4337</a>
 */
class NacosPropertySourceRefreshListenerTest {

	private NacosConfigManager nacosConfigManager;

	private ConfigService configService;

	private NacosPropertySourceRefreshListener listener;

	private MutablePropertySources propertySources;

	@BeforeEach
	void setup() {
		nacosConfigManager = Mockito.mock(NacosConfigManager.class);
		configService = Mockito.mock(ConfigService.class);
		ConfigurableApplicationContext applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
		ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
		propertySources = new MutablePropertySources();
		NacosConfigProperties props = new NacosConfigProperties();
		when(nacosConfigManager.getConfigService()).thenReturn(configService);
		when(nacosConfigManager.getNacosConfigProperties()).thenReturn(props);
		when(applicationContext.getEnvironment()).thenReturn(environment);
		when(environment.getPropertySources()).thenReturn(propertySources);
		when(applicationContext.containsBean("nacosConfigSpringCloudRefreshEventListener")).thenReturn(false);
		listener = new NacosPropertySourceRefreshListener(nacosConfigManager);
		listener.setApplicationContext(applicationContext);
		listener.handle(Mockito.mock(ApplicationReadyEvent.class));
	}

	@Test
	void refreshUsesYmlSuffix() throws Exception {
		addSource("app.yml", "DEFAULT_GROUP", "yml");
		when(configService.getConfig("app.yml", "DEFAULT_GROUP", 3000L))
			.thenReturn("key: value");

		refresh("app.yml", "DEFAULT_GROUP");

		NacosPropertySource refreshed = getSource("app.yml", "DEFAULT_GROUP");
		assertThat(refreshed.getSuffix()).isEqualTo("yml");
		assertThat(refreshed.getProperty("key")).isEqualTo("value");
	}

	@Test
	void refreshUsesJsonSuffix() throws Exception {
		addSource("app.json", "DEFAULT_GROUP", "json");
		when(configService.getConfig("app.json", "DEFAULT_GROUP", 3000L))
			.thenReturn(null);

		refresh("app.json", "DEFAULT_GROUP");

		NacosPropertySource refreshed = getSource("app.json", "DEFAULT_GROUP");
		assertThat(refreshed.getSuffix()).isEqualTo("json");
	}

	@Test
	void refreshFallsBackToPropertiesWhenSuffixIsNull() throws Exception {
		NacosPropertySource ps = new NacosPropertySource(
				Collections.emptyList(), "DEFAULT_GROUP", "app", new Date(), true);
		propertySources.addFirst(ps);
		when(configService.getConfig("app", "DEFAULT_GROUP", 3000L))
			.thenReturn("key=value");

		refresh("app", "DEFAULT_GROUP");

		NacosPropertySource refreshed = getSource("app", "DEFAULT_GROUP");
		assertThat(refreshed.getSuffix()).isEqualTo("properties");
		assertThat(refreshed.getProperty("key")).isEqualTo("value");
	}

	@Test
	void consecutiveRefreshesPreserveYmlSuffix() throws Exception {
		String dataId = "app.yml";
		String group = "DEFAULT_GROUP";
		addSource(dataId, group, "yml");

		when(configService.getConfig(dataId, group, 3000L))
			.thenReturn("key: first", "key: second");

		refresh(dataId, group);
		NacosPropertySource firstReplacement = getSource(dataId, group);
		assertThat(firstReplacement.getSuffix()).isEqualTo("yml");
		assertThat(firstReplacement.getProperty("key")).isEqualTo("first");

		refresh(dataId, group);
		NacosPropertySource secondReplacement = getSource(dataId, group);
		assertThat(secondReplacement.getSuffix()).isEqualTo("yml");
		assertThat(secondReplacement.getProperty("key")).isEqualTo("second");
	}

	private void addSource(String dataId, String group, String suffix) {
		NacosPropertySource ps = new NacosPropertySource(
				Collections.emptyList(), group, dataId, new Date(), true);
		ps.setSuffix(suffix);
		propertySources.addFirst(ps);
	}

	private void refresh(String dataId, String group) {
		NacosConfigRefreshEvent event = new NacosConfigRefreshEvent(this, null,
				String.join(NacosConfigProperties.COMMAS, dataId, group));
		event.setDataId(dataId);
		event.setGroup(group);
		listener.handle(event);
	}

	private NacosPropertySource getSource(String dataId, String group) {
		return (NacosPropertySource) propertySources
			.get(String.join(NacosConfigProperties.COMMAS, dataId, group));
	}

}
