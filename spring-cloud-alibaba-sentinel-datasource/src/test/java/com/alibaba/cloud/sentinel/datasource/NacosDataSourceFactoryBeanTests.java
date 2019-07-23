/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.sentinel.datasource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.alibaba.cloud.sentinel.datasource.converter.SentinelConverter;
import com.alibaba.cloud.sentinel.datasource.factorybean.NacosDataSourceFactoryBean;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class NacosDataSourceFactoryBeanTests {

	private String dataId = "sentinel";
	private String groupId = "DEFAULT_GROUP";
	private String serverAddr = "localhost:8848";
	private String accessKey = "ak";
	private String secretKey = "sk";
	private String endpoint = "endpoint";
	private String namespace = "namespace";

	@Test
	public void testNacosFactoryBeanServerAddr() throws Exception {
		NacosDataSourceFactoryBean factoryBean = spy(new NacosDataSourceFactoryBean());

		Converter converter = mock(SentinelConverter.class);

		factoryBean.setDataId(dataId);
		factoryBean.setGroupId(groupId);
		factoryBean.setServerAddr(serverAddr);
		factoryBean.setConverter(converter);

		NacosDataSource nacosDataSource = mock(NacosDataSource.class);

		doReturn(nacosDataSource).when(factoryBean).getObject();
		when(nacosDataSource.readSource()).thenReturn("{}");

		assertEquals("NacosDataSourceFactoryBean getObject was wrong", nacosDataSource,
				factoryBean.getObject());
		assertEquals("NacosDataSource read source value was wrong", "{}",
				factoryBean.getObject().readSource());
		assertEquals("NacosDataSource converter was wrong", converter,
				factoryBean.getConverter());
		assertEquals("NacosDataSourceFactoryBean dataId was wrong", dataId,
				factoryBean.getDataId());
		assertEquals("NacosDataSourceFactoryBean groupId was wrong", groupId,
				factoryBean.getGroupId());
		assertEquals("NacosDataSourceFactoryBean serverAddr was wrong", serverAddr,
				factoryBean.getServerAddr());
	}

	@Test
	public void testNacosFactoryBeanProperties() throws Exception {
		NacosDataSourceFactoryBean factoryBean = spy(new NacosDataSourceFactoryBean());

		Converter converter = mock(SentinelConverter.class);

		factoryBean.setDataId(dataId);
		factoryBean.setGroupId(groupId);
		factoryBean.setAccessKey(accessKey);
		factoryBean.setSecretKey(secretKey);
		factoryBean.setEndpoint(endpoint);
		factoryBean.setNamespace(namespace);
		factoryBean.setConverter(converter);

		NacosDataSource nacosDataSource = mock(NacosDataSource.class);

		doReturn(nacosDataSource).when(factoryBean).getObject();
		when(nacosDataSource.readSource()).thenReturn("{}");

		assertEquals("NacosDataSourceFactoryBean getObject was wrong", nacosDataSource,
				factoryBean.getObject());
		assertEquals("NacosDataSource read source value was wrong", "{}",
				factoryBean.getObject().readSource());
		assertEquals("NacosDataSource converter was wrong", converter,
				factoryBean.getConverter());
		assertEquals("NacosDataSourceFactoryBean dataId was wrong", dataId,
				factoryBean.getDataId());
		assertEquals("NacosDataSourceFactoryBean groupId was wrong", groupId,
				factoryBean.getGroupId());
		assertEquals("NacosDataSourceFactoryBean namespace was wrong", namespace,
				factoryBean.getNamespace());
		assertEquals("NacosDataSourceFactoryBean endpoint was wrong", endpoint,
				factoryBean.getEndpoint());
		assertEquals("NacosDataSourceFactoryBean ak was wrong", accessKey,
				factoryBean.getAccessKey());
		assertEquals("NacosDataSourceFactoryBean sk was wrong", secretKey,
				factoryBean.getSecretKey());

	}

}
