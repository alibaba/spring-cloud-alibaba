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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NacosSnapshotConfigManagerTest {

	@Test
	void getAndRemoveConfigSnapshotConsumesSnapshotOnlyOnce() {
		NacosSnapshotConfigManager.putConfigSnapshot("test.properties",
				"DEFAULT_GROUP", "name=snapshot");
		try {
			assertThat(NacosSnapshotConfigManager.getAndRemoveConfigSnapshot(
					"test.properties", "DEFAULT_GROUP")).isEqualTo("name=snapshot");
			assertThat(NacosSnapshotConfigManager.getAndRemoveConfigSnapshot(
					"test.properties", "DEFAULT_GROUP")).isNull();
		}
		finally {
			NacosSnapshotConfigManager.removeConfigSnapshot("test.properties",
					"DEFAULT_GROUP");
		}
	}

	@Test
	void configSnapshotKeyIncludesNamespace() {
		NacosSnapshotConfigManager.putConfigSnapshot("namespace-a", "test.properties",
				"DEFAULT_GROUP", "name=a");
		NacosSnapshotConfigManager.putConfigSnapshot("namespace-b", "test.properties",
				"DEFAULT_GROUP", "name=b");
		try {
			assertThat(NacosSnapshotConfigManager.getAndRemoveConfigSnapshot(
					"namespace-a", "test.properties", "DEFAULT_GROUP")).isEqualTo(
					"name=a");
			assertThat(NacosSnapshotConfigManager.getAndRemoveConfigSnapshot(
					"namespace-b", "test.properties", "DEFAULT_GROUP")).isEqualTo(
					"name=b");
		}
		finally {
			NacosSnapshotConfigManager.removeConfigSnapshot("namespace-a",
					"test.properties", "DEFAULT_GROUP");
			NacosSnapshotConfigManager.removeConfigSnapshot("namespace-b",
					"test.properties", "DEFAULT_GROUP");
		}
	}

	@Test
	void defaultNamespaceUsesDefaultSnapshotKey() {
		NacosSnapshotConfigManager.putConfigSnapshot("public", "test.properties",
				"DEFAULT_GROUP", "name=snapshot");
		try {
			assertThat(NacosSnapshotConfigManager.getAndRemoveConfigSnapshot(
					"test.properties", "DEFAULT_GROUP")).isEqualTo("name=snapshot");
		}
		finally {
			NacosSnapshotConfigManager.removeConfigSnapshot("public", "test.properties",
					"DEFAULT_GROUP");
			NacosSnapshotConfigManager.removeConfigSnapshot("test.properties",
					"DEFAULT_GROUP");
		}
	}

}
