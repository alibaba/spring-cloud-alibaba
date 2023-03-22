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
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * Convert yaml file to {@link PropertySource}, support multi-document yaml file.
 *
 * @author Freeman
 */
public class YamlFileProcessor implements FileProcessor {
	private static final Logger log = LoggerFactory.getLogger(YamlFileProcessor.class);

	private static final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

	@Override
	public boolean hit(String fileName) {
		return Arrays.stream(loader.getFileExtensions()).anyMatch(fileName::endsWith);
	}

	@Override
	public EnumerablePropertySource<?> generate(String name, String content) {
		Resource resource = new ByteArrayResource(
				content.getBytes(StandardCharsets.UTF_8));
		CompositePropertySource propertySource = new CompositePropertySource(name);
		try {
			List<PropertySource<?>> sources = loader.load(name, resource);
			propertySource.getPropertySources().addAll(sources);
		}
		catch (IOException e) {
			log.warn("Failed to parse yaml file", e);
		}
		return propertySource;
	}
}
