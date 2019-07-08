/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alicloud.context.acm;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.alibaba.cloud.context.AliCloudServerMode;
import com.alibaba.cloud.context.acm.AcmConfiguration;

/**
 * acm properties
 *
 * @author leijuan
 * @author xiaolongzuo
 */
@ConfigurationProperties(prefix = "spring.cloud.alicloud.acm")
public class AcmProperties implements AcmConfiguration {

	private AliCloudServerMode serverMode = AliCloudServerMode.LOCAL;

	private String serverList = "127.0.0.1";

	private String serverPort = "8080";

	/**
	 * diamond group
	 */
	private String group = "DEFAULT_GROUP";

	/**
	 * timeout to get configuration
	 */
	private int timeout = 3000;

	/**
	 * the AliYun endpoint for ACM
	 */
	private String endpoint;

	/**
	 * ACM namespace
	 */
	private String namespace;

	/**
	 * name of ram role granted to ECS
	 */
	private String ramRoleName;

	private String fileExtension = "properties";

	private boolean refreshEnabled = true;

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	@Override
	public String getServerList() {
		return serverList;
	}

	public void setServerList(String serverList) {
		this.serverList = serverList;
	}

	@Override
	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	@Override
	public boolean isRefreshEnabled() {
		return refreshEnabled;
	}

	public void setRefreshEnabled(boolean refreshEnabled) {
		this.refreshEnabled = refreshEnabled;
	}

	@Override
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public String getRamRoleName() {
		return ramRoleName;
	}

	public void setRamRoleName(String ramRoleName) {
		this.ramRoleName = ramRoleName;
	}

	@Override
	public AliCloudServerMode getServerMode() {
		return serverMode;
	}

	public void setServerMode(AliCloudServerMode serverMode) {
		this.serverMode = serverMode;
	}
}
