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
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NacosPropertySourceRefreshListener}.
 */
public class NacosPropertySourceRefreshListenerTest {

	private static final String DATA_ID = "application.yaml";

	private static final String GROUP = "DEFAULT_GROUP";

	private NacosConfigManager nacosConfigManager;

	private ConfigService configService;

	private ConfigurableApplicationContext applicationContext;

	private MutablePropertySources propertySources;

	private NacosPropertySourceRefreshListener listener;

	@BeforeEach
	void setUp() {
		nacosConfigManager = Mockito.mock(NacosConfigManager.class);
		configService = Mockito.mock(ConfigService.class);
		NacosConfigProperties configProperties = new NacosConfigProperties();
		configProperties.setFileExtension("properties");
		Mockito.when(nacosConfigManager.getConfigService()).thenReturn(configService);
		Mockito.when(nacosConfigManager.getNacosConfigProperties())
				.thenReturn(configProperties);

		ConfigurableEnvironment environment = new StandardEnvironment();
		propertySources = environment.getPropertySources();

		applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
		Mockito.when(applicationContext.getEnvironment()).thenReturn(environment);
		Mockito.when(applicationContext.containsBean(
				"nacosConfigSpringCloudRefreshEventListener")).thenReturn(false);

		listener = new NacosPropertySourceRefreshListener(nacosConfigManager);
		listener.setApplicationContext(applicationContext);
		// Mark the listener as ready so handle(NacosConfigRefreshEvent) does its
		// work; a Mockito mock of ApplicationReadyEvent is enough because
		// handle(ApplicationReadyEvent) only flips the readiness flag.
		listener.handle(Mockito.mock(ApplicationReadyEvent.class));
	}

	@Test
	void refreshReusesYamlExtensionFromPreviousSource() throws Exception {
		NacosPropertySource prev = new NacosPropertySource(Collections.emptyList(),
				GROUP, DATA_ID, new Date(), true, "yaml");
		propertySources.addFirst(prev);

		Mockito.when(configService.getConfig(ArgumentMatchers.eq(DATA_ID),
				ArgumentMatchers.eq(GROUP), ArgumentMatchers.anyLong()))
				.thenReturn("foo:\n  bar: baz");

		NacosConfigRefreshEvent event = new NacosConfigRefreshEvent(this, null,
				"test");
		event.setDataId(DATA_ID);
		event.setGroup(GROUP);
		listener.handle(event);

		String sourceName = String.join(NacosConfigProperties.COMMAS, DATA_ID, GROUP);
		PropertySource<?> replaced = propertySources.get(sourceName);
		assertThat(replaced).isInstanceOf(NacosPropertySource.class);
		NacosPropertySource newSource = (NacosPropertySource) replaced;
		assertThat(newSource.getFileExtension()).isEqualTo("yaml");
		// The yaml content was parsed as yaml, producing a nested key — not a
		// single bogus "foo:\n  bar" key as would happen under the old
		// hard-coded "properties" path.
		assertThat(newSource.getProperty("foo.bar")).isEqualTo("baz");
	}

	@Test
	void refreshReusesJsonExtensionFromPreviousSource() throws Exception {
		NacosPropertySource prev = new NacosPropertySource(Collections.emptyList(),
				GROUP, "conf.json", new Date(), true, "json");
		propertySources.addFirst(prev);

		Mockito.when(configService.getConfig(ArgumentMatchers.eq("conf.json"),
				ArgumentMatchers.eq(GROUP), ArgumentMatchers.anyLong()))
				.thenReturn("{\"foo\":\"bar\"}");

		NacosConfigRefreshEvent event = new NacosConfigRefreshEvent(this, null,
				"test");
		event.setDataId("conf.json");
		event.setGroup(GROUP);
		listener.handle(event);

		String sourceName = String.join(NacosConfigProperties.COMMAS, "conf.json",
				GROUP);
		NacosPropertySource newSource = (NacosPropertySource) propertySources
				.get(sourceName);
		assertThat(newSource.getFileExtension()).isEqualTo("json");
		assertThat(newSource.getProperty("foo")).isEqualTo("bar");
	}

	@Test
	void refreshFallsBackToPropertiesWhenPreviousSourceHasNoExtension() throws Exception {
		// Previous source built via the legacy constructor — no fileExtension.
		NacosPropertySource prev = new NacosPropertySource(Collections.emptyList(),
				GROUP, DATA_ID, new Date(), true);
		propertySources.addFirst(prev);

		Mockito.when(configService.getConfig(ArgumentMatchers.eq(DATA_ID),
				ArgumentMatchers.eq(GROUP), ArgumentMatchers.anyLong()))
				.thenReturn("foo=bar");

		NacosConfigRefreshEvent event = new NacosConfigRefreshEvent(this, null,
				"test");
		event.setDataId(DATA_ID);
		event.setGroup(GROUP);
		listener.handle(event);

		String sourceName = String.join(NacosConfigProperties.COMMAS, DATA_ID, GROUP);
		NacosPropertySource newSource = (NacosPropertySource) propertySources
				.get(sourceName);
		assertThat(newSource.getFileExtension()).isEqualTo("properties");
		assertThat(newSource.getProperty("foo")).isEqualTo("bar");
	}

	@Test
	void refreshSkipsWhenDataIdIsMissing() throws Exception {
		NacosPropertySource prev = new NacosPropertySource(Collections.emptyList(),
				GROUP, DATA_ID, new Date(), true, "yaml");
		propertySources.addFirst(prev);

		NacosConfigRefreshEvent event = new NacosConfigRefreshEvent(this, null,
				"test");
		event.setGroup(GROUP);
		listener.handle(event);

		Mockito.verify(configService, Mockito.never()).getConfig(
				ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				ArgumentMatchers.anyLong());
	}

}
