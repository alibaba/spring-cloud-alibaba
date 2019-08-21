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

package com.alibaba.cloud.nacos.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.alibaba.nacos.client.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author zkz
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
		Map<String, String> map = parseJSON2Map(data);
		return this.generateProperties(this.reloadMap(map));
	}

	/**
	 * JSON to Map
	 */
	public static Map<String, String> parseJSON2Map(String json) throws IOException {
		Map<String, String> map = new HashMap<>(32);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(json);
		if (null == jsonNode) {
			return map;
		}
		parseJsonNode(map, jsonNode, "");
		return map;
	}

	private static void parseJsonNode(Map<String, String> jsonMap, JsonNode jsonNode,
			String parentKey) {
		Iterator<String> fieldNames = jsonNode.fieldNames();
		while (fieldNames.hasNext()) {
			String name = fieldNames.next();
			String fullKey = StringUtils.isEmpty(parentKey) ? name
					: parentKey + DOT + name;
			JsonNode resultValue = jsonNode.findValue(name);
			if (null == resultValue) {
				continue;
			}
			if (resultValue.isArray()) {
				Iterator<JsonNode> iterator = resultValue.elements();
				while (iterator != null && iterator.hasNext()) {
					JsonNode next = iterator.next();
					if (null == next) {
						continue;
					}
					parseJsonNode(jsonMap, next, fullKey);
				}
				continue;
			}
			if (resultValue.isObject()) {
				parseJsonNode(jsonMap, resultValue, fullKey);
				continue;
			}
			jsonMap.put(fullKey, resultValue.asText());
		}
	}

}
