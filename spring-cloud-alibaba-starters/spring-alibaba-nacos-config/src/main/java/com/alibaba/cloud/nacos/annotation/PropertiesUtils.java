/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.nacos.annotation;

import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import com.alibaba.nacos.common.utils.StringUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

final class PropertiesUtils {

	private PropertiesUtils() {
	}

	public static Properties convertToProperties(String content) throws Exception {
		if (StringUtils.isBlank(content)) {
			return new Properties();
		}
		try {
			return convertFormYamlContent(content);
		}
		catch (Exception e) {
			return convertFormPropertiesContent(content);
		}
	}

	private static Properties convertFormPropertiesContent(String content) throws Exception {
		Properties properties = new Properties();
		properties.load(new StringReader(content));
		return properties;
	}

	private static Properties convertFormYamlContent(String content) {

		Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
		Map<String, Object> yamlMap = yaml.load(content);

		Properties properties = new Properties();
		flattenMap("", yamlMap, properties);

		return properties;
	}

	private static void flattenMap(String prefix, Map<String, Object> map, Properties properties) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key =
					prefix.isEmpty() ? String.valueOf(entry.getKey()) : prefix + "." + String.valueOf(entry.getKey());
			if (entry.getValue() instanceof Map) {
				flattenMap(key, (Map<String, Object>) entry.getValue(), properties);
			}
			else {
				properties.setProperty(key, entry.getValue().toString());
			}
		}
	}
}
