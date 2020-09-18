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

import com.alibaba.cloud.sentinel.datasource.factorybean.ConsulDataSourceFactoryBean;

import org.springframework.util.StringUtils;

/**
 * Consul Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link ConsulDataSourceFactoryBean}.
 *
 * @author <a href="mailto:mengjindc@gmail.com">mengjin</a>
 */
public class ConsulDataSourceProperties extends AbstractDataSourceProperties {

	public ConsulDataSourceProperties() {
		super(ConsulDataSourceFactoryBean.class.getName());
	}

	/**
	 * consul server host.
	 */
	private String host;

	/**
	 * consul server port.
	 */
	private int port = 8500;

	/**
	 * data key in Redis.
	 */
	private String ruleKey;

	/**
	 * Request of query will hang until timeout (in second) or get updated value.
	 */
	private int waitTimeoutInSecond = 1;

	@Override
	public void preCheck(String dataSourceName) {
		if (StringUtils.isEmpty(host)) {
			throw new IllegalArgumentException("ConsulDataSource server-host is empty");
		}
		if (StringUtils.isEmpty(ruleKey)) {
			throw new IllegalArgumentException(
					"ConsulDataSource ruleKey can not be empty");
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

	public String getRuleKey() {
		return ruleKey;
	}

	public void setRuleKey(String ruleKey) {
		this.ruleKey = ruleKey;
	}

	public int getWaitTimeoutInSecond() {
		return waitTimeoutInSecond;
	}

	public void setWaitTimeoutInSecond(int waitTimeoutInSecond) {
		this.waitTimeoutInSecond = waitTimeoutInSecond;
	}

}
