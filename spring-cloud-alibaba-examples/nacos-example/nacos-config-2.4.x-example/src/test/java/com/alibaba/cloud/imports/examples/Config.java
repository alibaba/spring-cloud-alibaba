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

package com.alibaba.cloud.imports.examples;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author freeman
 */
@Configuration
public class Config {

	@Autowired
	private NacosConfigManager nacosConfigManager;

	@EventListener(ApplicationReadyEvent.class)
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void applicationReadyEventApplicationListener() throws NacosException {
		pushConfig2Nacos(nacosConfigManager.getConfigService());
	}

	private static void pushConfig2Nacos(ConfigService configService)
			throws NacosException {
		configService.publishConfig("test.yml", "DEFAULT_GROUP",
				"configdata:\n" +
						"  user:\n" +
						"    age: 21\n" +
						"    name: freeman\n" +
						"    map:\n" +
						"      hobbies:\n" +
						"        - art\n" +
						"        - programming\n" +
						"      intro: Hello, I'm freeman\n" +
						"    users:\n" +
						"      - name: dad\n" +
						"        age: 20\n" +
						"      - name: mom\n" +
						"        age: 18", "yaml");
	}
}
