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

package com.alibaba.cloud.sentinel.datasource.config;

import javax.validation.constraints.NotEmpty;

import com.alibaba.cloud.sentinel.datasource.factorybean.NacosDataSourceFactoryBean;

import org.springframework.util.StringUtils;

/**
 * Nacos Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link NacosDataSourceFactoryBean}.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class NacosDataSourceProperties extends AbstractDataSourceProperties {

	private String serverAddr;

	@NotEmpty
	private String groupId = "DEFAULT_GROUP";

	@NotEmpty
	private String dataId;

	private String endpoint;

	private String namespace;

	private String accessKey;

	private String secretKey;

	public NacosDataSourceProperties() {
		super(NacosDataSourceFactoryBean.class.getName());
	}

	@Override
	public void preCheck(String dataSourceName) {
		if (StringUtils.isEmpty(serverAddr)) {
			serverAddr = this.getEnv().getProperty(
					"spring.cloud.sentinel.datasource.nacos.server-addr",
					"localhost:8848");
		}
	}

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

}
