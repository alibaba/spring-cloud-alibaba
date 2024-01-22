package com.alibaba.cloud.nacos.refresh;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: ruansheng
 * @date: 2024-01-22
 */
public class NacosSnapshotConfigManager {

	private static final Map<String, String> CONFIG_INFO_SNAPSHOT_MAP = new ConcurrentHashMap<>(8);


	private static String formatConfigSnapshotKey(String dataId, String group) {
		return dataId + "@" + group;
	}

	public static String getConfigSnapshot(String dataId, String group) {
		return CONFIG_INFO_SNAPSHOT_MAP.get(formatConfigSnapshotKey(dataId, group));
	}

	public static void putConfigSnapshot(String dataId, String group, String configInfo) {
		CONFIG_INFO_SNAPSHOT_MAP.put(formatConfigSnapshotKey(dataId, group), configInfo);
	}

	public static void removeConfigSnapshot(String dataId, String group) {
		CONFIG_INFO_SNAPSHOT_MAP.remove(formatConfigSnapshotKey(dataId, group));
	}

}
