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

package com.alibaba.cloud.nacos.annotation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NacosPropertiesKeyListener} array key matching.
 *
 * @see <a href="https://github.com/alibaba/spring-cloud-alibaba/issues/4239">#4239</a>
 */
class NacosPropertiesKeyListenerTest {

	@Test
	void exactKeyMatch() {
		AtomicBoolean called = new AtomicBoolean(false);
		NacosPropertiesKeyListener listener = createListener(called, Set.of("myKey"), Set.of());

		listener.receiveConfigChange(buildEvent("myKey"));

		assertThat(called.get()).isTrue();
	}

	@Test
	void arrayIndexedKeyMatchesBaseKey() {
		AtomicBoolean called = new AtomicBoolean(false);
		NacosPropertiesKeyListener listener = createListener(called, Set.of("storeCodes"), Set.of());

		listener.receiveConfigChange(buildEvent("storeCodes[3]"));

		assertThat(called.get()).isTrue();
	}

	@Test
	void arrayIndexedKeyWithNestedPathMatchesBaseKey() {
		AtomicBoolean called = new AtomicBoolean(false);
		NacosPropertiesKeyListener listener = createListener(called, Set.of("config.items"), Set.of());

		listener.receiveConfigChange(buildEvent("config.items[0].name"));

		assertThat(called.get()).isTrue();
	}

	@Test
	void unrelatedKeyDoesNotMatch() {
		AtomicBoolean called = new AtomicBoolean(false);
		NacosPropertiesKeyListener listener = createListener(called, Set.of("myKey"), Set.of());

		listener.receiveConfigChange(buildEvent("otherKey[0]"));

		assertThat(called.get()).isFalse();
	}

	@Test
	void prefixMatchStillWorks() {
		AtomicBoolean called = new AtomicBoolean(false);
		NacosPropertiesKeyListener listener = createListener(called, Set.of(), Set.of("app.config"));

		listener.receiveConfigChange(buildEvent("app.config.items[0]"));

		assertThat(called.get()).isTrue();
	}

	@Test
	void emptyInterestedKeysPassesAllChanges() {
		AtomicBoolean called = new AtomicBoolean(false);
		NacosPropertiesKeyListener listener = createListener(called, Set.of(), Set.of());

		listener.receiveConfigChange(buildEvent("anyKey"));

		assertThat(called.get()).isTrue();
	}

	private NacosPropertiesKeyListener createListener(AtomicBoolean called, Set<String> keys, Set<String> prefixes) {
		Set<String> interestedKeys = keys.isEmpty() ? null : new HashSet<>(keys);
		Set<String> interestedPrefixes = prefixes.isEmpty() ? null : new HashSet<>(prefixes);
		return new NacosPropertiesKeyListener(new Object(), interestedKeys, interestedPrefixes) {
			@Override
			public void configChanged(ConfigChangeEvent event) {
				called.set(true);
			}
		};
	}

	private ConfigChangeEvent buildEvent(String key) {
		Map<String, ConfigChangeItem> data = new HashMap<>();
		ConfigChangeItem item = new ConfigChangeItem(key, null, "newValue");
		data.put(key, item);
		return new ConfigChangeEvent(data);
	}
}
