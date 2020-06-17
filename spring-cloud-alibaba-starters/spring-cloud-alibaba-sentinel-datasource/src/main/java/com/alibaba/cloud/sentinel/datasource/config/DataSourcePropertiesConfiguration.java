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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.util.ObjectUtils;

/**
 * Using By ConfigurationProperties.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see NacosDataSourceProperties
 * @see ApolloDataSourceProperties
 * @see ZookeeperDataSourceProperties
 * @see FileDataSourceProperties
 * @see RedisDataSourceProperties
 * @see ConsulDataSourceProperties
 */
public class DataSourcePropertiesConfiguration {

	private FileDataSourceProperties file;

	private NacosDataSourceProperties nacos;

	private ZookeeperDataSourceProperties zk;

	private ApolloDataSourceProperties apollo;

	private RedisDataSourceProperties redis;

	private ConsulDataSourceProperties consul;

	public DataSourcePropertiesConfiguration() {
	}

	public DataSourcePropertiesConfiguration(ConsulDataSourceProperties consul) {
		this.consul = consul;
	}

	public ConsulDataSourceProperties getConsul() {
		return consul;
	}

	public void setConsul(ConsulDataSourceProperties consul) {
		this.consul = consul;
	}

	public DataSourcePropertiesConfiguration(FileDataSourceProperties file) {
		this.file = file;
	}

	public DataSourcePropertiesConfiguration(NacosDataSourceProperties nacos) {
		this.nacos = nacos;
	}

	public DataSourcePropertiesConfiguration(ZookeeperDataSourceProperties zk) {
		this.zk = zk;
	}

	public DataSourcePropertiesConfiguration(ApolloDataSourceProperties apollo) {
		this.apollo = apollo;
	}

	public DataSourcePropertiesConfiguration(RedisDataSourceProperties redis) {
		this.redis = redis;
	}

	public FileDataSourceProperties getFile() {
		return file;
	}

	public void setFile(FileDataSourceProperties file) {
		this.file = file;
	}

	public NacosDataSourceProperties getNacos() {
		return nacos;
	}

	public void setNacos(NacosDataSourceProperties nacos) {
		this.nacos = nacos;
	}

	public ZookeeperDataSourceProperties getZk() {
		return zk;
	}

	public void setZk(ZookeeperDataSourceProperties zk) {
		this.zk = zk;
	}

	public ApolloDataSourceProperties getApollo() {
		return apollo;
	}

	public void setApollo(ApolloDataSourceProperties apollo) {
		this.apollo = apollo;
	}

	public RedisDataSourceProperties getRedis() {
		return redis;
	}

	public void setRedis(RedisDataSourceProperties redis) {
		this.redis = redis;
	}

	@JsonIgnore
	public List<String> getValidField() {
		return Arrays.stream(this.getClass().getDeclaredFields()).map(field -> {
			try {
				if (!ObjectUtils.isEmpty(field.get(this))) {
					return field.getName();
				}
				return null;
			}
			catch (IllegalAccessException e) {
				// won't happen
			}
			return null;
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@JsonIgnore
	public AbstractDataSourceProperties getValidDataSourceProperties() {
		List<String> invalidFields = getValidField();
		if (invalidFields.size() == 1) {
			try {
				this.getClass().getDeclaredField(invalidFields.get(0))
						.setAccessible(true);
				return (AbstractDataSourceProperties) this.getClass()
						.getDeclaredField(invalidFields.get(0)).get(this);
			}
			catch (IllegalAccessException e) {
				// won't happen
			}
			catch (NoSuchFieldException e) {
				// won't happen
			}
		}
		return null;
	}

}
