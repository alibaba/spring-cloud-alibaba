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
import com.alibaba.cloud.sentinel.datasource.factorybean.ZookeeperDataSourceFactoryBean;
import org.jspecify.annotations.Nullable;

import org.springframework.core.env.Environment;


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

	private @Nullable String path;

	private @Nullable String groupId;

	private @Nullable String dataId;

	@Override
	public void preCheck(String dataSourceName) {
		if (StringUtils.isEmpty(serverAddr)) {
			Environment env = this.getEnv();
			if (env != null) {
				serverAddr = env.getProperty("spring.cloud.sentinel.datasource.zk.server-addr", "");
			}
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

	public @Nullable String getPath() {
		return path;
	}

	public void setPath(@Nullable String path) {
		this.path = path;
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

}
