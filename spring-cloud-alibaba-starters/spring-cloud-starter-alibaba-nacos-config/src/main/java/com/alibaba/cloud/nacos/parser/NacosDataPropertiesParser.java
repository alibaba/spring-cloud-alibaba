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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * @author zkz
 */
public class NacosDataPropertiesParser extends AbstractNacosDataParser {

	private static final String NAME = "NACOS";

	private static final Logger log = LoggerFactory
			.getLogger(NacosDataPropertiesParser.class);

	public NacosDataPropertiesParser() {
		super("properties");
	}

	@Override
	protected Map<String, Object> doParse(String data) throws IOException {
		Resource resource = new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8));
		PropertiesPropertySourceLoader loader = new PropertiesPropertySourceLoader();
		List<PropertySource<?>> list = loader.load(NAME, resource);
		if (list.isEmpty()) {
			log.warn("The current configuration resolution result is empty");
			return Collections.emptyMap();
		}
		return (Map<String, Object>) list.get(0).getSource();
	}

}
