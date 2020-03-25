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

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} for creating {@link ZookeeperDataSource} instance.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see ZookeeperDataSource
 */
public class ZookeeperDataSourceFactoryBean implements FactoryBean<ZookeeperDataSource> {

	private String serverAddr;

	private String path;

	private String groupId;

	private String dataId;

	private Converter converter;

	@Override
	public ZookeeperDataSource getObject() throws Exception {
		if (StringUtils.isNotEmpty(groupId) && StringUtils.isNotEmpty(dataId)) {
			// the path will be /{groupId}/{dataId}
			return new ZookeeperDataSource(serverAddr, groupId, dataId, converter);
		}
		else {
			// using path directly
			return new ZookeeperDataSource(serverAddr, path, converter);
		}
	}

	@Override
	public Class<?> getObjectType() {
		return ZookeeperDataSource.class;
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

	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

}
