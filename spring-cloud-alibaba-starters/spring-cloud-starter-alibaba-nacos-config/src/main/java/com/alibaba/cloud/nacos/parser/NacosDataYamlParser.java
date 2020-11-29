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

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

/**
 * @author zkz
 */
public class NacosDataYamlParser extends AbstractNacosDataParser {

	public NacosDataYamlParser() {
		super(",yml,yaml,");
	}

	@Override
	protected Map<String, Object> doParse(String data) {
		YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
		yamlFactory.setResources(new ByteArrayResource(data.getBytes()));

		Map properties = yamlFactory.getObject();
		Map<String, Object> result = new LinkedHashMap<>(properties);
		return result;
	}

}
