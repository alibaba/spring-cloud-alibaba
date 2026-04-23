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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.core.env.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NacosPropertySource}.
 */
public class NacosPropertySourceTest {

	@Test
	void fileExtensionIsNullWhenUsingLegacyConstructor() {
		List<PropertySource<?>> sources = Collections.emptyList();
		NacosPropertySource source = new NacosPropertySource(sources, "DEFAULT_GROUP",
				"app.yaml", new Date(), true);
		assertThat(source.getFileExtension()).isNull();
	}

	@Test
	void fileExtensionIsPreservedWhenProvided() {
		List<PropertySource<?>> sources = Collections.emptyList();
		NacosPropertySource source = new NacosPropertySource(sources, "DEFAULT_GROUP",
				"app.yaml", new Date(), true, "yaml");
		assertThat(source.getFileExtension()).isEqualTo("yaml");
	}

	@Test
	void fileExtensionCanBeNullExplicitly() {
		List<PropertySource<?>> sources = Collections.emptyList();
		NacosPropertySource source = new NacosPropertySource(sources, "DEFAULT_GROUP",
				"app", new Date(), true, null);
		assertThat(source.getFileExtension()).isNull();
	}

	@Test
	void otherPropertiesAreNotAffectedByNewConstructor() {
		Date timestamp = new Date();
		List<PropertySource<?>> sources = Collections.emptyList();
		NacosPropertySource source = new NacosPropertySource(sources, "MY_GROUP",
				"app.json", timestamp, false, "json");
		assertThat(source.getGroup()).isEqualTo("MY_GROUP");
		assertThat(source.getDataId()).isEqualTo("app.json");
		assertThat(source.getTimestamp()).isEqualTo(timestamp);
		assertThat(source.isRefreshable()).isFalse();
		assertThat(source.getFileExtension()).isEqualTo("json");
	}

}
