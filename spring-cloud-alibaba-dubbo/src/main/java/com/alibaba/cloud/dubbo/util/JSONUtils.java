/*
 * Copyright (C) 2018 the original author or authors.
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
package com.alibaba.cloud.dubbo.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.dubbo.common.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * JSON Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class JSONUtils {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ObjectMapper objectMapper = new ObjectMapper();

	@PostConstruct
	public void init() {
		this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	public String toJSON(Collection<URL> urls) {
		return toJSON(urls.stream().map(URL::toFullString).collect(Collectors.toSet()));
	}

	public String toJSON(Object object) {
		String jsonContent = null;
		try {
			jsonContent = objectMapper.writeValueAsString(object);
		}
		catch (JsonProcessingException e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e);
			}
		}
		return jsonContent;
	}

	public List<URL> toURLs(String urlsJSON) {
		List<String> list = toList(urlsJSON);
		return list.stream().map(URL::valueOf).collect(Collectors.toList());
	}

	public List<String> toList(String json) {
		List<String> list = Collections.emptyList();
		try {
			if (StringUtils.hasText(json)) {
				list = objectMapper.readValue(json, List.class);
			}
		}
		catch (IOException e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e);
			}
		}
		return list;
	}
}
