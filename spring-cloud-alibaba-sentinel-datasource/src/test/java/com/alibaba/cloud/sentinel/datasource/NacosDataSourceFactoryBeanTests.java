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

package com.alibaba.cloud.sentinel.datasource;

import com.alibaba.cloud.sentinel.datasource.converter.SentinelConverter;
import com.alibaba.cloud.sentinel.datasource.factorybean.NacosDataSourceFactoryBean;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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

		assertThat(factoryBean.getObject()).isEqualTo(nacosDataSource);
		assertThat(factoryBean.getObject().readSource()).isEqualTo("{}");
		assertThat(factoryBean.getConverter()).isEqualTo(converter);
		assertThat(factoryBean.getDataId()).isEqualTo(dataId);
		assertThat(factoryBean.getGroupId()).isEqualTo(groupId);
		assertThat(factoryBean.getServerAddr()).isEqualTo(serverAddr);
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

		assertThat(factoryBean.getObject()).isEqualTo(nacosDataSource);
		assertThat(factoryBean.getObject().readSource()).isEqualTo("{}");
		assertThat(factoryBean.getConverter()).isEqualTo(converter);
		assertThat(factoryBean.getDataId()).isEqualTo(dataId);
		assertThat(factoryBean.getGroupId()).isEqualTo(groupId);
		assertThat(factoryBean.getNamespace()).isEqualTo(namespace);
		assertThat(factoryBean.getEndpoint()).isEqualTo(endpoint);
		assertThat(factoryBean.getAccessKey()).isEqualTo(accessKey);
		assertThat(factoryBean.getSecretKey()).isEqualTo(secretKey);
	}

}
