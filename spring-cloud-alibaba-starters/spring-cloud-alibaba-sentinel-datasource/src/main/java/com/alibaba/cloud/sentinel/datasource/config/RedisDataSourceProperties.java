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

import java.time.Duration;
import java.util.List;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.sentinel.datasource.factorybean.RedisDataSourceFactoryBean;
import org.jspecify.annotations.Nullable;


/**
 * Redis Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link RedisDataSourceFactoryBean}.
 *
 * @author <a href="mailto:wangiegie@gmail.com">lengleng</a>
 */
public class RedisDataSourceProperties extends AbstractDataSourceProperties {

	public RedisDataSourceProperties() {
		super(RedisDataSourceFactoryBean.class.getName());
	}

	/**
	 * redis server host.
	 */
	private String host = "localhost";

	/**
	 * redis server port.
	 */
	private int port = 6379;

	/**
	 * redis server password.
	 */
	private @Nullable String password;

	/**
	 * redis server default select database.
	 */
	private int database;

	/**
	 * redis server timeout.
	 */
	private @Nullable Duration timeout;

	/**
	 * Comma-separated list of "host:port" pairs.
	 */
	private @Nullable List<String> nodes;

	/**
	 * data key in Redis.
	 */
	private @Nullable String ruleKey;

	/**
	 * channel to subscribe in Redis.
	 */
	private @Nullable String channel;

	/**
	 * redis sentinel model.
	 */
	private @Nullable String masterId;

	@Override
	public void preCheck(String dataSourceName) {
		super.preCheck(dataSourceName);
		if (StringUtils.isEmpty(ruleKey)) {
			throw new IllegalArgumentException(
					"RedisDataSource  ruleKey can not be empty");
		}

		if (StringUtils.isEmpty(channel)) {
			throw new IllegalArgumentException(
					"RedisDataSource  channel can not be empty");
		}

		if (StringUtils.isEmpty(masterId)) {
			throw new IllegalArgumentException(
					"RedisDataSource  sentinel model，masterId can not be empty");
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public @Nullable String getRuleKey() {
		return ruleKey;
	}

	public void setRuleKey(@Nullable String ruleKey) {
		this.ruleKey = ruleKey;
	}

	public @Nullable String getChannel() {
		return channel;
	}

	public void setChannel(@Nullable String channel) {
		this.channel = channel;
	}

	public @Nullable String getPassword() {
		return password;
	}

	public void setPassword(@Nullable String password) {
		this.password = password;
	}

	public int getDatabase() {
		return database;
	}

	public void setDatabase(int database) {
		this.database = database;
	}

	public @Nullable Duration getTimeout() {
		return timeout;
	}

	public void setTimeout(@Nullable Duration timeout) {
		this.timeout = timeout;
	}

	public @Nullable List<String> getNodes() {
		return nodes;
	}

	public void setNodes(@Nullable List<String> nodes) {
		this.nodes = nodes;
	}

	public @Nullable String getMasterId() {
		return masterId;
	}

	public void setMasterId(@Nullable String masterId) {
		this.masterId = masterId;
	}

}
