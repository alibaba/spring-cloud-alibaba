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

import java.util.Set;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.OpenSergoDataSourceGroup;

import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} for creating {@link OpenSergoDataSourceGroup} instance.
 *
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @see OpenSergoDataSourceGroup
 */
public class OpenSergoDataSourceFactoryBean
		implements FactoryBean<OpenSergoDataSourceGroup> {

	private String host;

	private int port;

	private String namespace;

	private String app;

	private Set<String> enabledRules;

	private Converter converter;

	@Override
	public OpenSergoDataSourceGroup getObject() throws Exception {
		return new OpenSergoDataSourceGroup(host, port, namespace, app);
	}

	@Override
	public Class<?> getObjectType() {
		return OpenSergoDataSourceGroup.class;
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

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public Set<String> getEnabledRules() {
		return enabledRules;
	}

	public void setEnabledRules(Set<String> enabledRules) {
		this.enabledRules = enabledRules;
	}

	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

}
