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

package com.alibaba.cloud.examples.example;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Use the @Value annotation to get configuration example.
 *
 * @author lixiaoshuang
 */
@RestController
@RequestMapping("/nacos/annotation")
@RefreshScope
public class ValueAnnotationExample {

	@Value("${spring.cloud.nacos.config.serverAddr:}")
	private String serverAddr;

	@Value("${spring.cloud.nacos.config.prefix:}")
	private String prefix;

	@Value("${spring.cloud.nacos.config.group:}")
	private String group;

	@Value("${spring.cloud.nacos.config.namespace:}")
	private String namespace;

	@GetMapping
	public Map<String, String> getConfigInfo() {
		Map<String, String> result = new HashMap<>(4);
		result.put("serverAddr", serverAddr);
		result.put("prefix", prefix);
		result.put("group", group);
		result.put("namespace", namespace);
		return result;
	}
}
