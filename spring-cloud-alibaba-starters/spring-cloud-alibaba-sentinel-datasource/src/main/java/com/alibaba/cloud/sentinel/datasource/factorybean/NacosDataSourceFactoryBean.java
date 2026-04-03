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

package com.alibaba.cloud.sentinel.datasource.factorybean;

import java.util.Properties;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.nacos.api.PropertyKeyConst;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} for creating {@link NacosDataSource} instance.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see NacosDataSource
 */
public class NacosDataSourceFactoryBean implements FactoryBean<NacosDataSource> {

	private @Nullable String serverAddr;

	private @Nullable String contextPath;

	private @Nullable String username;

	private @Nullable String password;

	private @Nullable String groupId;

	private @Nullable String dataId;

	private @Nullable Converter converter;

	private @Nullable String endpoint;

	private @Nullable String namespace;

	private @Nullable String accessKey;

	private @Nullable String secretKey;

	@Override
	public NacosDataSource getObject() throws Exception {
		Properties properties = new Properties();
		if (!StringUtils.isEmpty(this.serverAddr)) {
			properties.setProperty(PropertyKeyConst.SERVER_ADDR, this.serverAddr);
		}
		else {
			properties.setProperty(PropertyKeyConst.ENDPOINT, this.endpoint);
		}

		if (!StringUtils.isEmpty(this.contextPath)) {
			properties.setProperty(PropertyKeyConst.CONTEXT_PATH, this.contextPath);
		}
		if (!StringUtils.isEmpty(this.accessKey)) {
			properties.setProperty(PropertyKeyConst.ACCESS_KEY, this.accessKey);
		}
		if (!StringUtils.isEmpty(this.secretKey)) {
			properties.setProperty(PropertyKeyConst.SECRET_KEY, this.secretKey);
		}
		if (!StringUtils.isEmpty(this.namespace)) {
			properties.setProperty(PropertyKeyConst.NAMESPACE, this.namespace);
		}
		if (!StringUtils.isEmpty(this.username)) {
			properties.setProperty(PropertyKeyConst.USERNAME, this.username);
		}
		if (!StringUtils.isEmpty(this.password)) {
			properties.setProperty(PropertyKeyConst.PASSWORD, this.password);
		}
		return new NacosDataSource(properties, groupId, dataId, converter);
	}

	@Override
	public Class<?> getObjectType() {
		return NacosDataSource.class;
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

	public @Nullable String getGroupId() {
		return groupId;
	}

	public void setGroupId(@Nullable String groupId) {
		this.groupId = groupId;
	}

	public @Nullable String getDataId() {
		return dataId;
	}

	public void setDataId(@Nullable String dataId) {
		this.dataId = dataId;
	}

	public @Nullable Converter getConverter() {
		return converter;
	}

	public void setConverter(@Nullable Converter converter) {
		this.converter = converter;
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
