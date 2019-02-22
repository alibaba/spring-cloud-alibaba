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

package org.springframework.cloud.alibaba.nacos;

import org.springframework.cloud.alibaba.nacos.client.NacosPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaojing
 * @author pbting
 */
public class NacosPropertySourceRepository {

	private final static ConcurrentHashMap<String, NacosPropertySource> nacosPropertySourceRepository = new ConcurrentHashMap<>();

	/**
	 * @return all nacos properties from application context
	 */
	public static List<NacosPropertySource> getAll() {
		List<NacosPropertySource> result = new ArrayList<>();
		result.addAll(nacosPropertySourceRepository.values());
		return result;
	}

	public static void collectNacosPropertySources(
			NacosPropertySource nacosPropertySource) {
		nacosPropertySourceRepository.putIfAbsent(nacosPropertySource.getDataId(),
				nacosPropertySource);
	}

	public static NacosPropertySource getNacosPropertySource(String dataId) {

		return nacosPropertySourceRepository.get(dataId);
	}
}
