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

package com.alibaba.cloud.nacos.bridge;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.alibaba.boot.nacos.config.properties.NacosConfigProperties;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class NacosBridgeUtils {

	private static final Set<String> FIELD_NAME = new HashSet<>();

	// According to nacos-boot filed attributes configured filter configuration object

	static {
		Field[] fields = NacosConfigProperties.class.getDeclaredFields();
		for (Field field : fields) {
			FIELD_NAME.add(field.getName());
		}
	}

	public static Properties filter(Properties properties) {
		Properties result = new Properties();
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			String key = String.valueOf(entry.getKey());
			if (key.contains("-")) {
				String[] subs = key.split("-");
				key = buildJavaField(subs);
			}
			for (String fileName : FIELD_NAME) {
				if (key.contains(fileName)) {
					result.put(entry.getKey(), entry.getValue());
					break;
				}
			}
		}
		return result;
	}

	private NacosBridgeUtils() {
	}

	private static String buildJavaField(String[] subs) {
		StringBuilder sb = new StringBuilder();
		sb.append(subs[0]);
		for (int i = 1; i < subs.length; i++) {
			char[] chars = subs[i].toCharArray();
			chars[0] = Character.toUpperCase(chars[0]);
			sb.append(chars);
		}
		return sb.toString();
	}

}
