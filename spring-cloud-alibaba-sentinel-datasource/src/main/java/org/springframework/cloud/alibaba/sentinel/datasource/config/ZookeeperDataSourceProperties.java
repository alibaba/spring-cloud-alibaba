/*
 * Copyright (C) 2018 the original author or authors.
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

package org.springframework.cloud.alibaba.sentinel.datasource.config;

import javax.validation.constraints.NotNull;

import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.ZookeeperDataSourceFactoryBean;

/**
 * Zookeeper Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link ZookeeperDataSourceFactoryBean}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ZookeeperDataSourceProperties extends AbstractDataSourceProperties {

	public ZookeeperDataSourceProperties() {
		super(ZookeeperDataSourceFactoryBean.class.getName());
	}

	@NotNull
	private String serverAddr;

	private String path;

	private String groupId;

	private String dataId;

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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
}
