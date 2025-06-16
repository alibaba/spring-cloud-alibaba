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

package com.alibaba.cloud.rpc.config;


import java.util.HashMap;
import java.util.Map;

import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.rpc.RpcProperties;
import com.alibaba.cloud.rpc.server.RpcNettyServerListener;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author :Lictory
 * @date : 2024/08/12
 */

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = RpcProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class RpcRegistryConfiguration {

	@Autowired
	private NacosRegistration nacosRegistration;
	@Autowired
	private RpcProperties rpcProperties;

	@PostConstruct
	public void init() {
		Map<String, String> metadata = new HashMap<>();
		metadata.put(RpcProperties.NETTY_PORT_PREFIX, String.valueOf(rpcProperties.getPort()));
		nacosRegistration.getMetadata().putAll(metadata);
	}

	@Bean
	@ConditionalOnMissingBean
	public RpcNettyServerListener initRpcNettyServerListener() {
		return new RpcNettyServerListener();
	}
}
