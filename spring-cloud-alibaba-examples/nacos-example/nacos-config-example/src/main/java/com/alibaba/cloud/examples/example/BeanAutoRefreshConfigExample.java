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

import com.alibaba.cloud.examples.model.NacosConfigInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dynamic bean refresh example.
 *
 * @author lixiaoshuang
 */
@RestController
@RequestMapping("/nacos/bean")
public class BeanAutoRefreshConfigExample {

	@Autowired
	private NacosConfigInfo nacosConfigInfo;

	@GetMapping
	public Map<String, String> getConfigInfo() {
		Map<String, String> result = new HashMap<>();
		result.put("serverAddr", nacosConfigInfo.getServerAddr());
		result.put("prefix", nacosConfigInfo.getPrefix());
		result.put("group", nacosConfigInfo.getGroup());
		result.put("namespace", nacosConfigInfo.getNamespace());
		return result;
	}

}
