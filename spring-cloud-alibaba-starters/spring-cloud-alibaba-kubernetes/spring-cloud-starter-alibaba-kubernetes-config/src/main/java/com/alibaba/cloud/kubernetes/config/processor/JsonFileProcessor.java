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

package com.alibaba.cloud.kubernetes.config.processor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;

/**
 * Convert JSON string to {@link PropertySource}, support JSON array.
 *
 * @author Freeman
 */
public class JsonFileProcessor implements FileProcessor {
	private static final Logger log = LoggerFactory.getLogger(JsonFileProcessor.class);

	private static final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

	private static final Yaml yaml = new Yaml();

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public boolean hit(String fileName) {
		return fileName.endsWith(".json");
	}

	@Override
	public EnumerablePropertySource<?> generate(String name, String content) {
		if (content.trim().startsWith("{")) {
			// json object
			return convertJsonObjectStringToPropertySource(name, content);
		}
		CompositePropertySource result = new CompositePropertySource(name);
		try {
			List<?> list = objectMapper.readValue(content, List.class);
			if (list.isEmpty()) {
				return result;
			}
			for (int i = 0; i < list.size(); i++) {
				Object o = list.get(i);
				if (o instanceof Map) {
					// means it's a json object
					result.addPropertySource(convertJsonObjectStringToPropertySource(
							String.format("%s[%d]", name, i),
							objectMapper.writeValueAsString(o)));
				}
			}
		}
		catch (JsonProcessingException e) {
			log.warn("Failed to parse json file", e);
		}
		return result;
	}

	private static CompositePropertySource convertJsonObjectStringToPropertySource(
			String name, String jsonObjectString) {
		// We don't want to change the Spring default behavior
		// this is how we convert json to PropertySource
		// json -> java.util.Map -> yaml -> PropertySource
		Map<?, ?> map = new HashMap<>();
		try {
			map = objectMapper.readValue(jsonObjectString, Map.class);
		}
		catch (JsonProcessingException e) {
			log.warn("Failed to parse json file", e);
		}
		CompositePropertySource propertySource = new CompositePropertySource(name);
		try {
			String yamlString = yaml.dump(map);
			List<PropertySource<?>> pss = loader.load(name,
					new ByteArrayResource(yamlString.getBytes(StandardCharsets.UTF_8)));
			propertySource.getPropertySources().addAll(pss);
		}
		catch (IOException e) {
			log.warn("Failed to parse yaml file", e);
		}
		return propertySource;
	}
}
