/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.google.common.collect.Lists;

/**
 * @author xiaojing
 * @author pbting
 */
public class NacosPropertySourceRepository {

	private final static ConcurrentHashMap<String, NacosPropertySource> NACOS_PROPERTY_SOURCE_REPOSITORY = new ConcurrentHashMap<>();

	/**
	 * @return all nacos properties from application context
	 */
	public static List<NacosPropertySource> getAll() {
		return Lists.newArrayList(NACOS_PROPERTY_SOURCE_REPOSITORY.values());
	}

	public static void collectNacosPropertySources(
			NacosPropertySource nacosPropertySource) {
		NACOS_PROPERTY_SOURCE_REPOSITORY.putIfAbsent(nacosPropertySource.getDataId(),
				nacosPropertySource);
	}

	public static NacosPropertySource getNacosPropertySource(String dataId) {

		return NACOS_PROPERTY_SOURCE_REPOSITORY.get(dataId);
	}
}
