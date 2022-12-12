/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.governance.opensergo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(OpenSergoConfigProperties.PREFIX)
public class OpenSergoConfigProperties {

	/**
	 * Prefix in yaml.
	 */
	public static final String PREFIX = "spring.cloud.opensergo.config";

	private String host;

	private int port;

	private String namespace = "default";

	public String getNamespace() {
		return namespace;
	}

	void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
