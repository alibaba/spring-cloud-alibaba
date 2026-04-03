/*
 * Copyright 2013-present the original author or authors.
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

package com.alibaba.cloud.sentinel.datasource.config;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.sentinel.datasource.factorybean.NacosDataSourceFactoryBean;
import jakarta.validation.constraints.NotEmpty;
import org.jspecify.annotations.Nullable;

import org.springframework.core.env.Environment;

/**
 * Nacos Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link NacosDataSourceFactoryBean}.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class NacosDataSourceProperties extends AbstractDataSourceProperties {

	private @Nullable String serverAddr;

	private @Nullable String contextPath;

	private @Nullable String username;

	private @Nullable String password;

	@NotEmpty
	private String groupId = "DEFAULT_GROUP";

	@NotEmpty
	private @Nullable String dataId;

	private @Nullable String endpoint;

	private @Nullable String namespace;

	private @Nullable String accessKey;

	private @Nullable String secretKey;

	public NacosDataSourceProperties() {
		super(NacosDataSourceFactoryBean.class.getName());
	}

	@Override
	public void preCheck(String dataSourceName) {
		if (StringUtils.isEmpty(serverAddr)) {
			Environment env = this.getEnv();
			if (env != null) {
				serverAddr = env.getProperty(
						"spring.cloud.sentinel.datasource.nacos.server-addr",
						"127.0.0.1:8848");
			}
			else {
				serverAddr = "127.0.0.1:8848";
			}
		}
	}

	public @Nullable String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(@Nullable String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public @Nullable String getContextPath() {
		return contextPath;
	}

	public void setContextPath(@Nullable String contextPath) {
		this.contextPath = contextPath;
	}

	public @Nullable String getUsername() {
		return username;
	}

	public void setUsername(@Nullable String username) {
		this.username = username;
	}

	public @Nullable String getPassword() {
		return password;
	}

	public void setPassword(@Nullable String password) {
		this.password = password;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public @Nullable String getDataId() {
		return dataId;
	}

	public void setDataId(@Nullable String dataId) {
		this.dataId = dataId;
	}

	public @Nullable String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(@Nullable String endpoint) {
		this.endpoint = endpoint;
	}

	public @Nullable String getNamespace() {
		return namespace;
	}

	public void setNamespace(@Nullable String namespace) {
		this.namespace = namespace;
	}

	public @Nullable String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(@Nullable String accessKey) {
		this.accessKey = accessKey;
	}

	public @Nullable String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(@Nullable String secretKey) {
		this.secretKey = secretKey;
	}

}
