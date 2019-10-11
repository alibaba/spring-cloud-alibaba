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
import java.util.Properties;

/**
 * @author zkz
 */
public class NacosDataPropertiesParser extends AbstractNacosDataParser {

	public NacosDataPropertiesParser() {
		super("properties");
	}

	@Override
	protected Properties doParse(String data) throws IOException {
		Properties properties = new OrderedProperties();
		StringReader reader = new StringReader(data);
		BufferedReader br = new BufferedReader(reader);

		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] arr = line.split("=");
			if (arr.length != 2) {
				throw new IOException("error properties format");
			}

			properties.put(arr[0], arr[1]);
		}
		return properties;
	}

}
