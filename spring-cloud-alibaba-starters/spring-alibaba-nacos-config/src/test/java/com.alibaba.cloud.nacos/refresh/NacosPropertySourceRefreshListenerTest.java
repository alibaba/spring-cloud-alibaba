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

package com.alibaba.cloud.nacos.refresh;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link NacosPropertySourceRefreshListener}.
 *
 * @author wushiyuan
 */
public class NacosPropertySourceRefreshListenerTest {

	private NacosPropertySourceRefreshListener listener;
	private ConfigurableApplicationContext applicationContext;
	private ConfigurableEnvironment environment;
	private MutablePropertySources propertySources;
	private NacosConfigManager nacosConfigManager;
	private ConfigService configService;

	@BeforeEach
	public void setUp() throws NacosException {
		nacosConfigManager = mock(NacosConfigManager.class);
		configService = mock(ConfigService.class);
		NacosConfigProperties properties = new NacosConfigProperties();
		properties.setTimeout(3000);

		when(nacosConfigManager.getConfigService()).thenReturn(configService);
		when(nacosConfigManager.getNacosConfigProperties()).thenReturn(properties);

		applicationContext = mock(ConfigurableApplicationContext.class);
		environment = mock(ConfigurableEnvironment.class);
		propertySources = new MutablePropertySources();

		when(applicationContext.getEnvironment()).thenReturn(environment);
		when(environment.getPropertySources()).thenReturn(propertySources);
		when(applicationContext.containsBean("nacosConfigSpringCloudRefreshEventListener")).thenReturn(false);

		listener = new NacosPropertySourceRefreshListener(nacosConfigManager);
		listener.setApplicationContext(applicationContext);
	}

	/**
	 * Test refresh with ConfigData path naming (group@dataId).
	 * This test verifies that the listener can locate a NacosPropertySource
	 * registered under the ConfigData naming convention and refresh it.
	 */
	@Test
	public void testRefreshWithConfigDataPathNaming() throws NacosException {
		// Given: a property source with ConfigData path naming (group@dataId)
		String group = "DEFAULT_GROUP";
		String dataId = "test-config.yml";
		String configDataName = group + "@" + dataId;  // ConfigData path uses group@dataId

		Map<String, Object> initialData = new HashMap<>();
		initialData.put("app.name", "old-value");
		MapPropertySource innerSource = new MapPropertySource(configDataName, initialData);
		NacosPropertySource nacosPropertySource = new NacosPropertySource(
				Collections.singletonList(innerSource), group, dataId, new Date(), true, "yml");

		propertySources.addLast(nacosPropertySource);

		// Mark app as ready
		listener.handle(mock(ApplicationReadyEvent.class));

		// When: config changes in Nacos
		String newConfig = "app:\n  name: new-value";
		when(configService.getConfig(dataId, group, 3000L)).thenReturn(newConfig);

		NacosConfigRefreshEvent event = new NacosConfigRefreshEvent(this, null, "test refresh");
		event.setDataId(dataId);
		event.setGroup(group);

		listener.handle(event);

		// Then: the listener should have found the source via configDataName fallback
		// and replaced it. After replacement, the new NacosPropertySource uses
		// the standard "dataId,group" naming from its constructor.
		String standardName = dataId + "," + group;
		assertThat(propertySources.contains(standardName)).isTrue();
		Object updatedValue = propertySources.get(standardName).getProperty("app.name");
		assertThat(updatedValue).isEqualTo("new-value");
	}

	/**
	 * Test refresh with yml file extension.
	 * This test verifies that the listener uses the actual file extension
	 * from the existing NacosPropertySource instead of hardcoding "properties".
	 */
	@Test
	public void testRefreshWithYmlExtension() throws NacosException {
		// Given: a yml config
		String group = "DEFAULT_GROUP";
		String dataId = "test-config.yml";
		String sourceName = dataId + "," + group;  // bootstrap path naming

		Map<String, Object> initialData = new HashMap<>();
		initialData.put("app.port", "8080");
		MapPropertySource innerSource = new MapPropertySource(sourceName, initialData);
		NacosPropertySource nacosPropertySource = new NacosPropertySource(
				Collections.singletonList(innerSource), group, dataId, new Date(), true, "yml");

		propertySources.addLast(nacosPropertySource);

		// Mark app as ready
		listener.handle(mock(ApplicationReadyEvent.class));

		// When: yml config changes in Nacos
		String newYmlConfig = "app:\n  port: 9090";
		when(configService.getConfig(dataId, group, 3000L)).thenReturn(newYmlConfig);

		NacosConfigRefreshEvent event = new NacosConfigRefreshEvent(this, null, "test refresh");
		event.setDataId(dataId);
		event.setGroup(group);

		listener.handle(event);

		// Then: the yml should be parsed correctly (not as properties)
		assertThat(propertySources.contains(sourceName)).isTrue();
		Object updatedValue = propertySources.get(sourceName).getProperty("app.port");
		// YAML parser returns Integer for numeric values
		assertThat(String.valueOf(updatedValue)).isEqualTo("9090");
	}
}
