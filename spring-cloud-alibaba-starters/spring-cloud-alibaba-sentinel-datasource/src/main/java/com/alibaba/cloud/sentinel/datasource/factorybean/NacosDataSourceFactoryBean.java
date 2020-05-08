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

package com.alibaba.cloud.sentinel.datasource.factorybean;

import java.util.Properties;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.nacos.api.PropertyKeyConst;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.StringUtils;

/**
 * A {@link FactoryBean} for creating {@link NacosDataSource} instance.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see NacosDataSource
 */
public class NacosDataSourceFactoryBean implements FactoryBean<NacosDataSource> {

	private String serverAddr;

	private String username;

	private String password;

	private String groupId;

	private String dataId;

	private Converter converter;

	private String endpoint;

	private String namespace;

	private String accessKey;

	private String secretKey;

	@Override
	public NacosDataSource getObject() throws Exception {
		Properties properties = new Properties();
		if (!StringUtils.isEmpty(this.serverAddr)) {
			properties.setProperty(PropertyKeyConst.SERVER_ADDR, this.serverAddr);
		}
		else {
			properties.setProperty(PropertyKeyConst.ACCESS_KEY, this.accessKey);
			properties.setProperty(PropertyKeyConst.SECRET_KEY, this.secretKey);
			properties.setProperty(PropertyKeyConst.ENDPOINT, this.endpoint);
		}
		if (!StringUtils.isEmpty(this.namespace)) {
			properties.setProperty(PropertyKeyConst.NAMESPACE, this.namespace);
		}
		properties.setProperty(PropertyKeyConst.USERNAME, this.username);
		properties.setProperty(PropertyKeyConst.PASSWORD, this.password);
		return new NacosDataSource(properties, groupId, dataId, converter);
	}

	@Override
	public Class<?> getObjectType() {
		return NacosDataSource.class;
	}

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
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
