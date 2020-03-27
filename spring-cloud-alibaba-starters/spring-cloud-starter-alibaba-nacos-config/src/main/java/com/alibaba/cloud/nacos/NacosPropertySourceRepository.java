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

package com.alibaba.cloud.nacos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.nacos.client.NacosPropertySource;

/**
 * @author xiaojing
 * @author pbting
 */
public final class NacosPropertySourceRepository {

	private final static ConcurrentHashMap<String, NacosPropertySource> NACOS_PROPERTY_SOURCE_REPOSITORY = new ConcurrentHashMap<>();

	private NacosPropertySourceRepository() {

	}

	/**
	 * @return all nacos properties from application context.
	 */
	public static List<NacosPropertySource> getAll() {
		return new ArrayList<>(NACOS_PROPERTY_SOURCE_REPOSITORY.values());
	}

	/**
	 * recommend to use {@link NacosPropertySourceRepository#collectNacosPropertySource}.
	 * @param nacosPropertySource nacosPropertySource
	 */
	@Deprecated
	public static void collectNacosPropertySources(
			NacosPropertySource nacosPropertySource) {
		NACOS_PROPERTY_SOURCE_REPOSITORY.putIfAbsent(nacosPropertySource.getDataId(),
				nacosPropertySource);
	}

	/**
	 * recommend to use
	 * {@link NacosPropertySourceRepository#getNacosPropertySource(java.lang.String, java.lang.String)}.
	 * @param dataId dataId
	 * @return NacosPropertySource
	 */
	@Deprecated
	public static NacosPropertySource getNacosPropertySource(String dataId) {
		return NACOS_PROPERTY_SOURCE_REPOSITORY.get(dataId);
	}

	public static void collectNacosPropertySource(
			NacosPropertySource nacosPropertySource) {
		NACOS_PROPERTY_SOURCE_REPOSITORY
				.putIfAbsent(getMapKey(nacosPropertySource.getDataId(),
						nacosPropertySource.getGroup()), nacosPropertySource);
	}

	public static NacosPropertySource getNacosPropertySource(String dataId,
			String group) {
		return NACOS_PROPERTY_SOURCE_REPOSITORY.get(getMapKey(dataId, group));
	}

	public static String getMapKey(String dataId, String group) {
		return String.join(NacosConfigProperties.COMMAS, String.valueOf(dataId),
				String.valueOf(group));
	}

}
