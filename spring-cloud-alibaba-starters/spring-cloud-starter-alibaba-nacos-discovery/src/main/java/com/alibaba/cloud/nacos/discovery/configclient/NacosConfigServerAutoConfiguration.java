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

package com.alibaba.cloud.nacos.discovery.configclient;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Extra configuration for config server if it happens to be registered with Nacos.
 *
 * @author JevonYang
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass({ NacosDiscoveryProperties.class, ConfigServerProperties.class })
public class NacosConfigServerAutoConfiguration {

	private final NacosDiscoveryProperties properties;

	private final ConfigServerProperties server;

	public NacosConfigServerAutoConfiguration(
			ObjectProvider<NacosDiscoveryProperties> properties,
			ObjectProvider<ConfigServerProperties> server) {
		this.properties = properties.getIfAvailable();
		this.server = server.getIfAvailable();
	}

	@PostConstruct
	public void init() {
		if (this.properties == null || this.server == null) {
			return;
		}
		String prefix = this.server.getPrefix();
		if (StringUtils.hasText(prefix) && !StringUtils
				.hasText(this.properties.getMetadata().get("configPath"))) {
			this.properties.getMetadata().put("configPath", prefix);
		}
	}

}
