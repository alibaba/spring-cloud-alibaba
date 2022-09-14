/*
 * Copyright 2013-2022 the original author or authors.
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
 * Example of extended configuration. When a configuration in the shared configuration
 * does not meet the requirements, the extended configuration can be used to override the
 * shared configuration. Priority: Main Configuration &gt; Extended Configuration &gt;
 * Shared Configuration.
 *
 * @author lixiaoshuang
 */
@RestController
@RequestMapping("/nacos/extension/config")
@RefreshScope
public class ExtensionConfigExample {

	@Value("${spring.datasource.name:}")
	private String name;

	@Value("${spring.datasource.url:}")
	private String url;

	@Value("${spring.datasource.username:}")
	private String username;

	@Value("${spring.datasource.password:}")
	private String password;

	@Value("${spring.datasource.driverClassName:}")
	private String driverClassName;

	@GetMapping
	public Map<String, String> getConfigInfo() {
		Map<String, String> result = new HashMap<>(4);
		result.put("name", name);
		result.put("url", url);
		result.put("username", username);
		result.put("password", password);
		result.put("driverClassName", driverClassName);
		return result;
	}

}
