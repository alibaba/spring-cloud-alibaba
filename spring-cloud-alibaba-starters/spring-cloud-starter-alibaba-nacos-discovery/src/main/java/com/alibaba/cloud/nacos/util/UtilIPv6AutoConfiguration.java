/*
 * Copyright 2013-2024 the original author or authors.
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

package com.alibaba.cloud.nacos.util;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author HH
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
public class UtilIPv6AutoConfiguration {

	public UtilIPv6AutoConfiguration() {
	}

	@Bean
	@ConditionalOnMissingBean
	public InetIPv6Utils inetIPv6Utils(InetUtilsProperties properties) {
		return new InetIPv6Utils(properties);
	}

}
