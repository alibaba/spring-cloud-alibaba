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
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		String[] sourceArr;
		String[] valueArr;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			line = line.trim();
			if(StringUtils.startsWith(line,"#")){
				continue;
			}
			sourceArr = line.split("=");
			if (sourceArr.length < 2) {
				log.warn("ignore no properties format line : {}", line);
			}
			key = sourceArr[0].trim();
			valueArr = Arrays.copyOfRange(sourceArr, 1, sourceArr.length);
			value = String.join("", valueArr).trim();
			properties.put(key, value);
		}
		return properties;
	}

}
