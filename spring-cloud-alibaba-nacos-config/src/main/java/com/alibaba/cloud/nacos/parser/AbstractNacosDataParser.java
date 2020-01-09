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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.util.StringUtils;

/**
 * @author zkz
 */
public abstract class AbstractNacosDataParser {

	protected static final String DOT = ".";

	protected static final String VALUE = "value";

	private String extension;

	private AbstractNacosDataParser nextParser;

	protected AbstractNacosDataParser(String extension) {
		if (StringUtils.isEmpty(extension)) {
			throw new IllegalArgumentException("extension cannot be empty");
		}
		this.extension = extension.toLowerCase();
	}

	/**
	 * Verify dataId extensions.
	 * @param extension file extension. json or xml or yml or yaml or properties
	 * @return valid or not
	 */
	public final boolean checkFileExtension(String extension) {
		if (this.isLegal(extension.toLowerCase())) {
			return true;
		}
		if (this.nextParser == null) {
			return false;
		}
		return this.nextParser.checkFileExtension(extension);

	}

	/**
	 * Parsing nacos configuration content.
	 * @param data config data from Nacos
	 * @param extension file extension. json or xml or yml or yaml or properties
	 * @return result of Properties
	 * @throws IOException thrown if there is a problem parsing config.
	 */
	public final Properties parseNacosData(String data, String extension)
			throws IOException {
		if (extension == null || extension.length() < 1) {
			throw new IllegalStateException("The file extension cannot be empty");
		}
		if (this.isLegal(extension.toLowerCase())) {
			return this.doParse(data);
		}
		if (this.nextParser == null) {
			throw new IllegalStateException(getTips(extension));
		}
		return this.nextParser.parseNacosData(data, extension);
	}

	/**
	 * Core logic for parsing.
	 * @param data config from Nacos
	 * @return result of Properties
	 * @throws IOException thrown if there is a problem parsing config.
	 */
	protected abstract Properties doParse(String data) throws IOException;

	protected AbstractNacosDataParser setNextParser(AbstractNacosDataParser nextParser) {
		this.nextParser = nextParser;
		return this;
	}

	public AbstractNacosDataParser addNextParser(AbstractNacosDataParser nextParser) {
		if (this.nextParser == null) {
			this.nextParser = nextParser;
		}
		else {
			this.nextParser.addNextParser(nextParser);
		}
		return this;
	}

	protected boolean isLegal(String extension) {
		return this.extension.equalsIgnoreCase(extension)
				|| this.extension.contains(extension);
	}

	/**
	 * Generate key-value pairs from the map.
	 */
	protected Properties generateProperties(Map<String, Object> map) {
		if (null == map || map.isEmpty()) {
			return null;
		}
		Properties properties = new Properties();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			if (StringUtils.isEmpty(key)) {
				continue;
			}
			key = key.startsWith(DOT) ? key.replaceFirst("\\.", "") : key;
			properties.put(key, entry.getValue());
		}
		return properties;
	}

	/**
	 * Reload the key ending in `value` if need.
	 */
	protected Map<String, Object> reloadMap(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		Map<String, Object> result = new HashMap<>(map);
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			if (key.contains(DOT)) {
				int idx = key.lastIndexOf(DOT);
				String suffix = key.substring(idx + 1);
				if (VALUE.equalsIgnoreCase(suffix)) {
					result.put(key.substring(0, idx), entry.getValue());
				}
			}
		}
		return result;
	}

	public static String getTips(String fileName) {
		return String.format(
				"[%s] must contains file extension with properties|yaml|yml|xml|json",
				fileName);
	}

}
