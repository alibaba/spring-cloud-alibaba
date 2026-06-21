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
import com.alibaba.cloud.nacos.client.NacosPropertySourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for suffix-aware refresh in {@link NacosPropertySourceRefreshListener}.
 *
 * @see <a href="https://github.com/alibaba/spring-cloud-alibaba/issues/4337">#4337</a>
 */
class NacosPropertySourceRefreshListenerTest {

	private NacosConfigManager nacosConfigManager;

	private NacosPropertySourceBuilder builder;

	private NacosPropertySourceRefreshListener listener;

	private MutablePropertySources propertySources;

	@BeforeEach
	void setup() {
		nacosConfigManager = Mockito.mock(NacosConfigManager.class);
		builder = Mockito.mock(NacosPropertySourceBuilder.class);
		ConfigurableApplicationContext applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
		ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
		propertySources = new MutablePropertySources();
		NacosConfigProperties props = new NacosConfigProperties();
		when(nacosConfigManager.getNacosConfigProperties()).thenReturn(props);
		when(applicationContext.getEnvironment()).thenReturn(environment);
		when(environment.getPropertySources()).thenReturn(propertySources);
		when(applicationContext.containsBean("nacosConfigSpringCloudRefreshEventListener")).thenReturn(false);
		listener = new NacosPropertySourceRefreshListener(nacosConfigManager);
		listener.setApplicationContext(applicationContext);
		listener.handle(Mockito.mock(ApplicationReadyEvent.class));
	}

	@Test
	void refreshUsesYmlSuffix() {
		addSource("app.yml", "DEFAULT_GROUP", "yml");
		stubBuild("app.yml", "DEFAULT_GROUP");
		injectBuilder();
		NacosConfigRefreshEvent e1 = new NacosConfigRefreshEvent(this, null, "app.yml,DEFAULT_GROUP");
		e1.setDataId("app.yml");
		e1.setGroup("DEFAULT_GROUP");
		listener.handle(e1);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(builder).build(eq("app.yml"), eq("DEFAULT_GROUP"), captor.capture(), anyBoolean());
		assertThat(captor.getValue()).isEqualTo("yml");
	}

	@Test
	void refreshUsesJsonSuffix() {
		addSource("app.json", "DEFAULT_GROUP", "json");
		stubBuild("app.json", "DEFAULT_GROUP");
		injectBuilder();
		NacosConfigRefreshEvent e2 = new NacosConfigRefreshEvent(this, null, "app.json,DEFAULT_GROUP");
		e2.setDataId("app.json");
		e2.setGroup("DEFAULT_GROUP");
		listener.handle(e2);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(builder).build(eq("app.json"), eq("DEFAULT_GROUP"), captor.capture(), anyBoolean());
		assertThat(captor.getValue()).isEqualTo("json");
	}

	@Test
	void refreshFallsBackToPropertiesWhenSuffixIsNull() {
		NacosPropertySource ps = new NacosPropertySource(
				Collections.emptyList(), "DEFAULT_GROUP", "app", new Date(), true);
		propertySources.addFirst(ps);
		stubBuild("app", "DEFAULT_GROUP");
		injectBuilder();
		NacosConfigRefreshEvent e3 = new NacosConfigRefreshEvent(this, null, "app,DEFAULT_GROUP");
		e3.setDataId("app");
		e3.setGroup("DEFAULT_GROUP");
		listener.handle(e3);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(builder).build(eq("app"), eq("DEFAULT_GROUP"), captor.capture(), anyBoolean());
		assertThat(captor.getValue()).isEqualTo("properties");
	}

	private void addSource(String dataId, String group, String suffix) {
		NacosPropertySource ps = new NacosPropertySource(
				Collections.emptyList(), group, dataId, new Date(), true);
		ps.setSuffix(suffix);
		propertySources.addFirst(ps);
	}

	private void stubBuild(String dataId, String group) {
		NacosPropertySource result = new NacosPropertySource(
				Collections.emptyList(), group, dataId, new Date(), true);
		when(builder.build(eq(dataId), eq(group), anyString(), anyBoolean())).thenReturn(result);
	}

	private void injectBuilder() {
		try {
			var field = NacosPropertySourceRefreshListener.class
					.getDeclaredField("nacosPropertySourceBuilder");
			field.setAccessible(true);
			field.set(listener, builder);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
