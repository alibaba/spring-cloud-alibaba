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

import com.alibaba.cloud.sentinel.datasource.factorybean.ZookeeperDataSourceFactoryBean;

import org.springframework.util.StringUtils;

/**
 * Zookeeper Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link ZookeeperDataSourceFactoryBean}.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ZookeeperDataSourceProperties extends AbstractDataSourceProperties {

	public ZookeeperDataSourceProperties() {
		super(ZookeeperDataSourceFactoryBean.class.getName());
	}

	private String serverAddr = "localhost:2181";

	private String path;

	private String groupId;

	private String dataId;

	@Override
	public void preCheck(String dataSourceName) {
		if (StringUtils.isEmpty(serverAddr)) {
			serverAddr = this.getEnv()
					.getProperty("spring.cloud.sentinel.datasource.zk.server-addr", "");
			if (StringUtils.isEmpty(serverAddr)) {
				throw new IllegalArgumentException(
						"ZookeeperDataSource server-addr is empty");
			}
		}
	}

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
