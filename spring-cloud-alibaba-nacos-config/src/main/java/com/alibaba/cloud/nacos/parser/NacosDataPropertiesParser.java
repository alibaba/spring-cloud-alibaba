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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * @author zkz
 * @author element
 */
public class NacosDataPropertiesParser extends AbstractNacosDataParser {

	private static final Logger log = LoggerFactory
			.getLogger(NacosDataPropertiesParser.class);

	public NacosDataPropertiesParser() {
		super("properties");
	}

	@Override
	protected Properties doParse(String data) throws IOException {
		Properties properties = new OrderedProperties();
		StringReader reader = new StringReader(data);
		BufferedReader br = new BufferedReader(reader);

		String key;
		String value;
		int separator;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			line = line.trim();
			if (StringUtils.startsWith(line, "#")) {
				continue;
			}
			separator = line.indexOf("=");
			if (separator < 1) {
				log.warn("ignore no properties format line : {}", line);
				continue;
			}
			key = line.substring(0, separator).trim();
			value = line.substring(separator + 1).trim();
			properties.put(key, value);
		}
		return properties;
	}

}
