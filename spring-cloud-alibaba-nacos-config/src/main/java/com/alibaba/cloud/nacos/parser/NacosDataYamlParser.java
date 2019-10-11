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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * @author zkz
 */
public class NacosDataYamlParser extends AbstractNacosDataParser {

	public NacosDataYamlParser() {
		super(",yml,yaml,");
	}

	@Override
	protected Properties doParse(String data) {

		YamlMapFactoryBean y = new YamlMapFactoryBean();
		y.setResources(new Resource[] { new ByteArrayResource(data.getBytes()) });
		Map<String, Object> result = new LinkedHashMap();
		flattenedMap(result, y.getObject(), null);
		Properties properties = new OrderedProperties();

		for (Map.Entry<String, Object> entry : result.entrySet()) {
			properties.put(entry.getKey(), entry.getValue());
		}

		return properties;
	}

	private void flattenedMap(Map<String, Object> result,
			@Nullable Map<String, Object> source, @Nullable String path) {
		if (source != null) {
			source.forEach((key, value) -> {
				if (StringUtils.hasText(path)) {
					if (key.startsWith("[")) {
						key = path + key;
					}
					else {
						key = path + '.' + key;
					}
				}

				if (value instanceof String) {
					result.put(key, value);
				}
				else if (value instanceof Map) {
					Map<String, Object> map = (Map) value;
					this.flattenedMap(result, map, key);
				}
				else if (value instanceof Collection) {
					Collection<Object> collection = (Collection) value;
					if (collection.isEmpty()) {
						result.put(key, "");
					}
					else {
						int count = 0;
						Iterator iterator = collection.iterator();

						while (iterator.hasNext()) {
							Object object = iterator.next();
							this.flattenedMap(result,
									Collections.singletonMap("[" + count++ + "]", object),
									key);
						}
					}
				}
				else {
					result.put(key, value != null ? value : "");
				}

			});
		}
	}

}
