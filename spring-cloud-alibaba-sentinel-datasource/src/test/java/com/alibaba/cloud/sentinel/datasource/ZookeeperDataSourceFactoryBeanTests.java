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

import com.alibaba.cloud.sentinel.datasource.converter.XmlConverter;
import com.alibaba.cloud.sentinel.datasource.factorybean.ZookeeperDataSourceFactoryBean;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ZookeeperDataSourceFactoryBeanTests {

	private String dataId = "dataId";

	private String groupId = "groupId";

	private String serverAddr = "localhost:2181";

	private String path = "/sentinel";

	@Test
	public void testZKWithoutPathFactoryBean() throws Exception {
		ZookeeperDataSourceFactoryBean factoryBean = spy(
				ZookeeperDataSourceFactoryBean.class);

		Converter converter = mock(XmlConverter.class);

		ZookeeperDataSource zookeeperDataSource = mock(ZookeeperDataSource.class);

		factoryBean.setConverter(converter);
		factoryBean.setDataId(dataId);
		factoryBean.setGroupId(groupId);
		factoryBean.setServerAddr(serverAddr);

		when(zookeeperDataSource.readSource()).thenReturn("{}");
		doReturn(zookeeperDataSource).when(factoryBean).getObject();

		assertThat(factoryBean.getObject()).isEqualTo(zookeeperDataSource);
		assertThat(factoryBean.getObject().readSource()).isEqualTo("{}");
		assertThat(factoryBean.getDataId()).isEqualTo(dataId);
		assertThat(factoryBean.getConverter()).isEqualTo(converter);
		assertThat(factoryBean.getGroupId()).isEqualTo(groupId);
		assertThat(factoryBean.getServerAddr()).isEqualTo(serverAddr);
	}

	@Test
	public void testZKWithPathFactoryBean() throws Exception {
		ZookeeperDataSourceFactoryBean factoryBean = spy(
				ZookeeperDataSourceFactoryBean.class);

		Converter converter = mock(XmlConverter.class);

		ZookeeperDataSource zookeeperDataSource = mock(ZookeeperDataSource.class);

		factoryBean.setConverter(converter);
		factoryBean.setPath(path);
		factoryBean.setServerAddr(serverAddr);

		when(zookeeperDataSource.readSource()).thenReturn("{}");
		doReturn(zookeeperDataSource).when(factoryBean).getObject();

		assertThat(factoryBean.getObject()).isEqualTo(zookeeperDataSource);
		assertThat(factoryBean.getObject().readSource()).isEqualTo("{}");
		assertThat(factoryBean.getConverter()).isEqualTo(converter);
		assertThat(factoryBean.getPath()).isEqualTo(path);
		assertThat(factoryBean.getServerAddr()).isEqualTo(serverAddr);
	}

}
