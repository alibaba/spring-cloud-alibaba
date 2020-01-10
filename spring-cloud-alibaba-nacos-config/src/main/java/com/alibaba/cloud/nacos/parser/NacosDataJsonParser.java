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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author zkz
 * @author yuhuangbin
 */
public class NacosDataJsonParser extends AbstractNacosDataParser {

	protected NacosDataJsonParser() {
		super("json");
	}

	@Override
	protected Properties doParse(String data) throws IOException {
		if (StringUtils.isEmpty(data)) {
			return null;
		}
		Map<String, Object> map = parseJSON2Map(data);
		return this.generateProperties(this.reloadMap(map));
	}

	/**
	 * JSON to Map.
	 * @param json json data
	 * @return the map convert by json string
	 * @throws IOException thrown if there is a problem parsing config.
	 */
	public static Map<String, Object> parseJSON2Map(String json) throws IOException {
		Map<String, Object> result = new HashMap<>(32);

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> nacosDataMap = mapper.readValue(json, Map.class);

		if (CollectionUtils.isEmpty(nacosDataMap)) {
			return result;
		}
		parseNacosDataMap(result, nacosDataMap, "");
		return result;
	}

	private static void parseNacosDataMap(Map<String, Object> result,
			Map<String, Object> dataMap, String parentKey) {
		Set<Map.Entry<String, Object>> entries = dataMap.entrySet();
		for (Iterator<Map.Entry<String, Object>> iterator = entries.iterator(); iterator
				.hasNext();) {
			Map.Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			Object value = entry.getValue();

			String fullKey = StringUtils.isEmpty(parentKey) ? key : key.startsWith("[")
					? parentKey.concat(key) : parentKey.concat(DOT).concat(key);

			if (value instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) value;
				parseNacosDataMap(result, map, fullKey);
				continue;
			}
			else if (value instanceof Collection) {
				int count = 0;
				Collection<Object> collection = (Collection<Object>) value;
				for (Object object : collection) {
					parseNacosDataMap(result,
							Collections.singletonMap("[" + (count++) + "]", object),
							fullKey);
				}
				continue;
			}

			result.put(fullKey, value);
		}
	}

}
