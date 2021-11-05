/*
 * Copyright 2013-2018 the original author or authors.
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
import java.util.Map;

import com.alibaba.cloud.nacos.NacosConfigProperties;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojing
 * @author pbting
 */
public class NacosPropertySource extends MapPropertySource {

	/**
	 * Nacos Group.
	 */
	private final String group;

	/**
	 * Nacos dataID.
	 */
	private final String dataId;

	/**
	 * timestamp the property get.
	 */
	private final Date timestamp;

	/**
	 * Whether to support dynamic refresh for this Property Source.
	 */
	private final boolean isRefreshable;

	NacosPropertySource(String group, String dataId, Map<String, Object> source,
			Date timestamp, boolean isRefreshable) {
		super(String.join(NacosConfigProperties.COMMAS, dataId, group), source);
		this.group = group;
		this.dataId = dataId;
		this.timestamp = timestamp;
		this.isRefreshable = isRefreshable;
	}

	NacosPropertySource(List<PropertySource<?>> propertySources, String group,
			String dataId, Date timestamp, boolean isRefreshable) {
		this(group, dataId, getSourceMap(group, dataId, propertySources), timestamp,
				isRefreshable);
	}

	private static Map<String, Object> getSourceMap(String group, String dataId,
			List<PropertySource<?>> propertySources) {
		if (CollectionUtils.isEmpty(propertySources)) {
			return Collections.emptyMap();
		}
		// If only one, return the internal element, otherwise wrap it.
		if (propertySources.size() == 1) {
			PropertySource propertySource = propertySources.get(0);
			if (propertySource != null && propertySource.getSource() instanceof Map) {
				return (Map<String, Object>) propertySource.getSource();
			}
		}
		// If it is multiple, it will be returned as it is, and the internal elements
		// cannot be directly retrieved, so the user needs to implement the retrieval
		// logic by himself
		return Collections.singletonMap(
				String.join(NacosConfigProperties.COMMAS, dataId, group),
				propertySources);
	}

	public String getGroup() {
		return this.group;
	}

	public String getDataId() {
		return dataId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public boolean isRefreshable() {
		return isRefreshable;
	}

}
