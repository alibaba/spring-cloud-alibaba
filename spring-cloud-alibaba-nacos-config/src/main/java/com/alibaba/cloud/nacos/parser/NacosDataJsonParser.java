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
import java.util.LinkedHashMap;
import java.util.Map;

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
	protected Map<String, Object> doParse(String data) throws IOException {
		if (StringUtils.isEmpty(data)) {
			return null;
		}
		Map<String, Object> map = parseJSON2Map(data);
		return this.reloadMap(map);
	}

	/**
	 * JSON to Map.
	 * @param json json data
	 * @return the map convert by json string
	 * @throws IOException thrown if there is a problem parsing config.
	 */
	private Map<String, Object> parseJSON2Map(String json) throws IOException {
		Map<String, Object> result = new LinkedHashMap<>(32);

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> nacosDataMap = mapper.readValue(json, LinkedHashMap.class);

		if (CollectionUtils.isEmpty(nacosDataMap)) {
			return result;
		}
		flattenedMap(result, nacosDataMap, EMPTY_STRING);
		return result;
	}

}
