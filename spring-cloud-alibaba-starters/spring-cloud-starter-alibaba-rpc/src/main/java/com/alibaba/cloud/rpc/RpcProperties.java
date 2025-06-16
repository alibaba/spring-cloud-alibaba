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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author :Lictory
 * @date : 2024/09/27
 */
@ConfigurationProperties("spring.cloud.rpc.netty")
public class RpcProperties {

	/**
	 * config prefix.
	 */
	public static final String PREFIX = "spring.cloud.rpc";

	/**
	 * prefix of netty port.
	 */
	public static final String NETTY_PORT_PREFIX = "spring.cloud.rpc.netty.port";

	/**
	 * prefix of netty host.
	 */
	public static final String HOST_PREFIX = "spring.cloud.rpc.netty.host";

	private Integer port;
	private String host;

	public Integer getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void setHost(String host) {
		this.host = host;
	}
}
