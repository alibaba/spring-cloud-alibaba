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
import com.alibaba.csp.sentinel.datasource.consul.ConsulDataSource;

import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} for creating {@link ConsulDataSource} instance.
 *
 * @author <a href="mailto:mengjindc@gmail.com">mengjin</a>
 * @see ConsulDataSource
 */
public class ConsulDataSourceFactoryBean implements FactoryBean<ConsulDataSource> {

	private String host;

	private int port;

	private String ruleKey;

	private int waitTimeoutInSecond;

	private Converter converter;

	@Override
	public ConsulDataSource getObject() throws Exception {
		return new ConsulDataSource(host, port, ruleKey, waitTimeoutInSecond, converter);
	}

	@Override
	public Class<?> getObjectType() {
		return ConsulDataSource.class;
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

	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

}
