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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.cloud.nacos.utils.NacosConfigUtils;

import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import static com.alibaba.cloud.nacos.parser.AbstractPropertySourceLoader.DOT;

/**
 * @author zkz
 */
public final class NacosDataParserHandler {

	/**
	 * default extension.
	 */
	private static final String DEFAULT_EXTENSION = "properties";

	private static List<PropertySourceLoader> propertySourceLoaders;

	private NacosDataParserHandler() {
		propertySourceLoaders = SpringFactoriesLoader
				.loadFactories(PropertySourceLoader.class, getClass().getClassLoader());
	}

	/**
	 * Parsing nacos configuration content.
	 * @param configName name of nacos-config
	 * @param configValue value from nacos-config
	 * @param extension identifies the type of configValue
	 * @return result of Map
	 * @throws IOException thrown if there is a problem parsing config.
	 */
	public List<PropertySource<?>> parseNacosData(String configName, String configValue,
			String extension) throws IOException {
		if (StringUtils.isEmpty(configValue)) {
			return Collections.emptyList();
		}
		if (StringUtils.isEmpty(extension)) {
			extension = this.getFileExtension(configName);
		}
		for (PropertySourceLoader propertySourceLoader : propertySourceLoaders) {
			if (!canLoadFileExtension(propertySourceLoader, extension)) {
				continue;
			}
			NacosByteArrayResource nacosByteArrayResource;
			if (propertySourceLoader instanceof PropertiesPropertySourceLoader) {
				// PropertiesPropertySourceLoader internal is to use the ISO_8859_1,
				// the Chinese will be garbled, needs to transform into unicode.
				nacosByteArrayResource = new NacosByteArrayResource(
						NacosConfigUtils.selectiveConvertUnicode(configValue).getBytes(),
						configName);
			}
			else {
				nacosByteArrayResource = new NacosByteArrayResource(
						configValue.getBytes(), configName);
			}
			nacosByteArrayResource.setFilename(getFileName(configName, extension));
			List<PropertySource<?>> propertySourceList = propertySourceLoader
					.load(configName, nacosByteArrayResource);
			if (CollectionUtils.isEmpty(propertySourceList)) {
				return Collections.emptyList();
			}
			return propertySourceList.stream().filter(Objects::nonNull)
					.map(propertySource -> {
						if (propertySource instanceof EnumerablePropertySource) {
							String[] propertyNames = ((EnumerablePropertySource) propertySource)
									.getPropertyNames();
							if (propertyNames != null && propertyNames.length > 0) {
								Map<String, Object> map = new LinkedHashMap<>();
								Arrays.stream(propertyNames).forEach(name -> {
									map.put(name, propertySource.getProperty(name));
								});
								return new OriginTrackedMapPropertySource(
										propertySource.getName(), map, true);
							}
						}
						return propertySource;
					}).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	/**
	 * check the current extension can be processed.
	 * @param loader the propertySourceLoader
	 * @param extension file extension
	 * @return if can match extension
	 */
	private boolean canLoadFileExtension(PropertySourceLoader loader, String extension) {
		return Arrays.stream(loader.getFileExtensions())
				.anyMatch((fileExtension) -> StringUtils.endsWithIgnoreCase(extension,
						fileExtension));
	}

	/**
	 * @param name filename
	 * @return file extension, default {@code DEFAULT_EXTENSION} if don't get
	 */
	public String getFileExtension(String name) {
		if (StringUtils.isEmpty(name)) {
			return DEFAULT_EXTENSION;
		}
		int idx = name.lastIndexOf(DOT);
		if (idx > 0 && idx < name.length() - 1) {
			return name.substring(idx + 1);
		}
		return DEFAULT_EXTENSION;
	}

	private String getFileName(String name, String extension) {
		if (StringUtils.isEmpty(extension)) {
			return name;
		}
		if (StringUtils.isEmpty(name)) {
			return extension;
		}
		int idx = name.lastIndexOf(DOT);
		if (idx > 0 && idx < name.length() - 1) {
			String ext = name.substring(idx + 1);
			if (extension.equalsIgnoreCase(ext)) {
				return name;
			}
		}
		return name + DOT + extension;
	}

	public static NacosDataParserHandler getInstance() {
		return ParserHandler.HANDLER;
	}

	private static class ParserHandler {

		private static final NacosDataParserHandler HANDLER = new NacosDataParserHandler();

	}

}
