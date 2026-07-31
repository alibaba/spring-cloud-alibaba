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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for suffix field on {@link NacosPropertySource}.
 *
 * @see <a href="https://github.com/alibaba/spring-cloud-alibaba/issues/4337">#4337</a>
 */
class NacosPropertySourceTest {

	@Test
	void suffixIsNullByDefault() {
		NacosPropertySource ps = new NacosPropertySource(
				Collections.emptyList(), "DEFAULT_GROUP", "app.yml", new Date(), true);
		assertThat(ps.getSuffix()).isNull();
	}

	@Test
	void setSuffixStoresValue() {
		NacosPropertySource ps = new NacosPropertySource(
				Collections.emptyList(), "DEFAULT_GROUP", "app.yml", new Date(), true);
		ps.setSuffix("yml");
		assertThat(ps.getSuffix()).isEqualTo("yml");
	}

	@Test
	void setSuffixSupportsAllFormats() {
		for (String suffix : new String[] { "yml", "yaml", "json", "xml", "properties" }) {
			NacosPropertySource ps = new NacosPropertySource(
					Collections.emptyList(), "DEFAULT_GROUP", "app", new Date(), true);
			ps.setSuffix(suffix);
			assertThat(ps.getSuffix()).isEqualTo(suffix);
		}
	}

}
