/*
 * Copyright 2022-2023 the original author or authors.
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

import javax.annotation.PostConstruct;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
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

	/**
	 * jwt token for istiod 15012 port.
	 */
	private String istiodToken;

	private Boolean logXds;

	@PostConstruct
	public void init() {
		if (this.port <= 0 || this.port > 65535) {
			this.port = IstioConstants.ISTIOD_SECURE_PORT;
		}
		if (StringUtils.isEmpty(host)) {
			this.host = IstioConstants.DEFAULT_ISTIOD_ADDR;
		}
		if (pollingPoolSize <= 0) {
			pollingPoolSize = IstioConstants.DEFAULT_POLLING_SIZE;
		}
		if (pollingTime <= 0) {
			pollingTime = IstioConstants.DEFAULT_POLLING_TIME;
		}
		if (logXds == null) {
			logXds = true;
		}
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

	public String getIstiodToken() {
		return istiodToken;
	}

	public void setIstiodToken(String istiodToken) {
		this.istiodToken = istiodToken;
	}

	public boolean isLogXds() {
		return Boolean.TRUE.equals(logXds);
	}

	public void setLogXds(boolean logXds) {
		this.logXds = logXds;
	}

}
