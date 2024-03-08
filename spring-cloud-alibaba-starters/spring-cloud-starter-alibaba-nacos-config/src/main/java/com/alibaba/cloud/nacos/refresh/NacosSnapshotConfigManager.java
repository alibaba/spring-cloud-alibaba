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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: ruansheng
 * @date: 2024-01-22
 */
public final class NacosSnapshotConfigManager {

	private NacosSnapshotConfigManager() {
	}

	private static final Logger log = LoggerFactory
			.getLogger(NacosSnapshotConfigManager.class);

	private static final Map<String, String> CONFIG_INFO_SNAPSHOT_MAP = new ConcurrentHashMap<>(
			8);

	private static final int MAX_SNAPSHOT_COUNT = 100;

	private static String formatConfigSnapshotKey(String dataId, String group) {
		return dataId + "@" + group;
	}

	public static String getAndRemoveConfigSnapshot(String dataId, String group) {
		String configInfo = CONFIG_INFO_SNAPSHOT_MAP
				.get(formatConfigSnapshotKey(dataId, group));
		removeConfigSnapshot(dataId, group);
		return configInfo;
	}

	public static void putConfigSnapshot(String dataId, String group, String configInfo) {
		try {
			// Theoretically, the capacity limit restriction will never be triggered.
			// This portion of the code serves as an additional fault tolerance layer.
			if (CONFIG_INFO_SNAPSHOT_MAP.size() > MAX_SNAPSHOT_COUNT) {
				Iterator<Map.Entry<String, String>> iterator = CONFIG_INFO_SNAPSHOT_MAP
						.entrySet().iterator();
				iterator.next();
				iterator.remove();
			}
			CONFIG_INFO_SNAPSHOT_MAP.put(formatConfigSnapshotKey(dataId, group),
					configInfo);
		}
		catch (Exception e) {
			log.warn("remove nacos config snapshot error", e);
		}
	}

	public static void removeConfigSnapshot(String dataId, String group) {
		CONFIG_INFO_SNAPSHOT_MAP.remove(formatConfigSnapshotKey(dataId, group));
	}

}
