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

package com.alibaba.cloud.sidecar;

import java.net.URI;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author www.itmuch.com
 */
@ConfigurationProperties("sidecar")
@Validated
public class SidecarProperties {

	/**
	 * polyglot service's ip.
	 */
	private String ip;

	/**
	 * polyglot service's port.
	 */
	@NotNull
	@Max(65535)
	@Min(1)
	private Integer port;

	/**
	 * polyglot service's health check url. this endpoint must return json and the format
	 * must follow spring boot actuator's health endpoint. eg. {"status": "UP"}.
	 */
	private URI healthCheckUrl;

	/**
	 * interval of health check.
	 */
	private long healthCheckInterval = 30000L;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public URI getHealthCheckUrl() {
		return healthCheckUrl;
	}

	public void setHealthCheckUrl(URI healthCheckUrl) {
		this.healthCheckUrl = healthCheckUrl;
	}

	public long getHealthCheckInterval() {
		return healthCheckInterval;
	}

	public void setHealthCheckInterval(long healthCheckInterval) {
		this.healthCheckInterval = healthCheckInterval;
	}

}
