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
public class NacosSnapshotConfigManager {

	private static final Logger log = LoggerFactory.getLogger(NacosSnapshotConfigManager.class);

	private static final Map<String, String> CONFIG_INFO_SNAPSHOT_MAP = new ConcurrentHashMap<>(8);
	private static final int MAX_SNAPSHOT_COUNT = 100;

	private static String formatConfigSnapshotKey(String dataId, String group) {
		return dataId + "@" + group;
	}

	public static String getConfigSnapshot(String dataId, String group) {
		return CONFIG_INFO_SNAPSHOT_MAP.get(formatConfigSnapshotKey(dataId, group));
	}

	public static void putConfigSnapshot(String dataId, String group, String configInfo) {
		try {
			// Theoretically, the capacity limit restriction will never be triggered.
			// This portion of the code logic serves as an additional fault tolerance layer.
			if (CONFIG_INFO_SNAPSHOT_MAP.size() > MAX_SNAPSHOT_COUNT) {
				Iterator<Map.Entry<String, String>> iterator = CONFIG_INFO_SNAPSHOT_MAP.entrySet().iterator();
				iterator.next();
				iterator.remove();
			}
			CONFIG_INFO_SNAPSHOT_MAP.put(formatConfigSnapshotKey(dataId, group), configInfo);
		}
		catch (Exception e) {
			log.warn("remove nacos config snapshot error", e);
		}
	}

	public static void removeConfigSnapshot(String dataId, String group) {
		CONFIG_INFO_SNAPSHOT_MAP.remove(formatConfigSnapshotKey(dataId, group));
	}

}
