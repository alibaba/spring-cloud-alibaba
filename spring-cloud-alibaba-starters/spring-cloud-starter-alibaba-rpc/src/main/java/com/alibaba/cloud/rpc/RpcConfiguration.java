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

package com.alibaba.cloud.rpc;

import com.alibaba.cloud.rpc.utils.UrlResolver;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


/**
 * @author :Lictory
 * @date : 2024/09/27
 */

@AutoConfiguration
@ConditionalOnProperty(prefix = RpcProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RpcProperties.class)
public class RpcConfiguration {
	@Bean
	@ConditionalOnMissingBean
	public RpcProperties rpcProperties() {
		return new RpcProperties();
	}


	@Bean
	@ConditionalOnMissingBean
	public UrlResolver initUrlResolver() {
		return new UrlResolver();
	}

}
