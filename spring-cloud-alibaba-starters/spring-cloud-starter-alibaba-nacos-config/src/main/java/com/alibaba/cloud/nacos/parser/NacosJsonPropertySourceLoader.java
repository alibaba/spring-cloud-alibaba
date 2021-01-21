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

package com.alibaba.cloud.nacos.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

/**
 * @author zkz
 */
public class NacosJsonPropertySourceLoader extends AbstractPropertySourceLoader {

	/**
	 * constant.
	 */
	private static final String VALUE = "value";

	/**
	 * Returns the file extensions that the loader supports (excluding the '.').
	 * @return the file extensions
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[] { "json" };
	}

	/**
	 * Load the resource into one or more property sources. Implementations may either
	 * return a list containing a single source, or in the case of a multi-document format
	 * such as yaml a source for each document in the resource.
	 * @param name the root name of the property source. If multiple documents are loaded
	 * an additional suffix should be added to the name for each source loaded.
	 * @param resource the resource to load
	 * @return a list property sources
	 * @throws IOException if the source cannot be loaded
	 */
	@Override
	protected List<PropertySource<?>> doLoad(String name, Resource resource)
			throws IOException {
		Map<String, Object> result = new LinkedHashMap<>(32);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> nacosDataMap = mapper.readValue(resource.getInputStream(),
				LinkedHashMap.class);
		flattenedMap(result, nacosDataMap, null);
		return Collections.singletonList(
				new OriginTrackedMapPropertySource(name, this.reloadMap(result), true));

	}

	/**
	 * Reload the key ending in `value` if need.
	 */
	protected Map<String, Object> reloadMap(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		Map<String, Object> result = new LinkedHashMap<>(map);
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			if (key.contains(DOT)) {
				int idx = key.lastIndexOf(DOT);
				String suffix = key.substring(idx + 1);
				if (VALUE.equalsIgnoreCase(suffix)) {
					result.put(key.substring(0, idx), entry.getValue());
				}
			}
		}
		return result;
	}

}
