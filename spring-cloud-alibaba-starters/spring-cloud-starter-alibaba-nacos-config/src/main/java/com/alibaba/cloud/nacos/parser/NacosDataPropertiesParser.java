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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.StringUtils;

/**
 * @author zkz
 */
public class NacosDataPropertiesParser extends AbstractNacosDataParser {

	private static final Logger log = LoggerFactory
			.getLogger(NacosDataPropertiesParser.class);

	public NacosDataPropertiesParser() {
		super("properties");
	}

	@Override
	protected Map<String, Object> doParse(String data) throws IOException {
		Map<String, Object> result = new LinkedHashMap<>();

		try (BufferedReader reader = new BufferedReader(new StringReader(data))) {
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				String dataLine = line.trim();
				if (StringUtils.isEmpty(dataLine) || dataLine.startsWith("#")) {
					continue;
				}
				int index = dataLine.indexOf("=");
				if (index == -1) {
					log.warn("the config data is invalid {}", dataLine);
					continue;
				}
				String key = dataLine.substring(0, index);
				String value = dataLine.substring(index + 1);
				result.put(key.trim(), value.trim());
			}
		}
		return result;
	}

}
