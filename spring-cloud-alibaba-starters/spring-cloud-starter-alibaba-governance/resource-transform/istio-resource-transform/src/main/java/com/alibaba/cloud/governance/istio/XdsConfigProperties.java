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

package com.alibaba.cloud.governance.istio;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(XdsConfigProperties.PREFIX)
public class XdsConfigProperties {

	/**
	 * Prefix in yaml.
	 */
	public static final String PREFIX = "spring.cloud.istio.config";

	private String host;

	private int port;

	private int pollingPoolSize;

	private int pollingTime;

	private boolean secure;

	private String caCert;

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

	public int getPollingPoolSize() {
		return pollingPoolSize;
	}

	public void setPollingPoolSize(int pollingPoolSize) {
		this.pollingPoolSize = pollingPoolSize;
	}

	public int getPollingTime() {
		return pollingTime;
	}

	public void setPollingTime(int pollingTime) {
		this.pollingTime = pollingTime;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public String getCaCert() {
		return caCert;
	}

	public void setCaCert(String caCert) {
		this.caCert = caCert;
	}

}
