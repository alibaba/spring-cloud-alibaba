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

package org.springframework.cloud.alibaba.sentinel.datasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.cloud.alibaba.sentinel.datasource.config.ApolloDataSourceProperties;
import org.springframework.cloud.alibaba.sentinel.datasource.config.DataSourcePropertiesConfiguration;
import org.springframework.cloud.alibaba.sentinel.datasource.config.FileDataSourceProperties;
import org.springframework.cloud.alibaba.sentinel.datasource.config.NacosDataSourceProperties;
import org.springframework.cloud.alibaba.sentinel.datasource.config.ZookeeperDataSourceProperties;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class DataSourcePropertiesConfigurationTests {

	@Test
	public void testFileAttr() {
		DataSourcePropertiesConfiguration dataSourcePropertiesConfiguration = new DataSourcePropertiesConfiguration();
		assertEquals("DataSourcePropertiesConfiguration valid field size was wrong", 0,
				dataSourcePropertiesConfiguration.getValidField().size());
		assertNull("DataSourcePropertiesConfiguration valid properties was not null",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());

		FileDataSourceProperties fileDataSourceProperties = buildFileProperties();

		dataSourcePropertiesConfiguration.setFile(fileDataSourceProperties);

		assertEquals(
				"DataSourcePropertiesConfiguration valid field size was wrong after set file attribute",
				1, dataSourcePropertiesConfiguration.getValidField().size());
		assertNotNull(
				"DataSourcePropertiesConfiguration file properties was null after set file attribute",
				dataSourcePropertiesConfiguration.getFile());
		assertNotNull(
				"DataSourcePropertiesConfiguration valid properties was null after set file attribute",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());
	}

	@Test
	public void testNacosAttr() {
		DataSourcePropertiesConfiguration dataSourcePropertiesConfiguration = new DataSourcePropertiesConfiguration();
		assertEquals("DataSourcePropertiesConfiguration valid field size was wrong", 0,
				dataSourcePropertiesConfiguration.getValidField().size());
		assertNull("DataSourcePropertiesConfiguration valid properties was not null",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());

		NacosDataSourceProperties nacosDataSourceProperties = buildNacosProperties();

		dataSourcePropertiesConfiguration.setNacos(nacosDataSourceProperties);

		assertEquals(
				"DataSourcePropertiesConfiguration valid field size was wrong after set nacos attribute",
				1, dataSourcePropertiesConfiguration.getValidField().size());
		assertNotNull(
				"DataSourcePropertiesConfiguration nacos properties was null after set nacos attribute",
				dataSourcePropertiesConfiguration.getNacos());
		assertNotNull(
				"DataSourcePropertiesConfiguration valid properties was null after set nacos attribute",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());
	}

	@Test
	public void testZKAttr() {
		DataSourcePropertiesConfiguration dataSourcePropertiesConfiguration = new DataSourcePropertiesConfiguration();
		assertEquals("DataSourcePropertiesConfiguration valid field size was wrong", 0,
				dataSourcePropertiesConfiguration.getValidField().size());
		assertNull("DataSourcePropertiesConfiguration valid properties was not null",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());

		ZookeeperDataSourceProperties zookeeperDataSourceProperties = buildZKProperties();

		dataSourcePropertiesConfiguration.setZk(zookeeperDataSourceProperties);

		assertEquals(
				"DataSourcePropertiesConfiguration valid field size was wrong after set zk attribute",
				1, dataSourcePropertiesConfiguration.getValidField().size());
		assertNotNull(
				"DataSourcePropertiesConfiguration zk properties was null after set zk attribute",
				dataSourcePropertiesConfiguration.getZk());
		assertNotNull(
				"DataSourcePropertiesConfiguration valid properties was null after set zk attribute",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());
	}

	@Test
	public void testApolloAttr() {
		DataSourcePropertiesConfiguration dataSourcePropertiesConfiguration = new DataSourcePropertiesConfiguration();
		assertEquals("DataSourcePropertiesConfiguration valid field size was wrong", 0,
				dataSourcePropertiesConfiguration.getValidField().size());
		assertNull("DataSourcePropertiesConfiguration valid properties was not null",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());

		ApolloDataSourceProperties apolloDataSourceProperties = buildApolloProperties();

		dataSourcePropertiesConfiguration.setApollo(apolloDataSourceProperties);

		assertEquals(
				"DataSourcePropertiesConfiguration valid field size was wrong after set apollo attribute",
				1, dataSourcePropertiesConfiguration.getValidField().size());
		assertNotNull(
				"DataSourcePropertiesConfiguration apollo properties was null after set apollo attribute",
				dataSourcePropertiesConfiguration.getApollo());
		assertNotNull(
				"DataSourcePropertiesConfiguration valid properties was null after set apollo attribute",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());
	}

	@Test
	public void testMultiAttr() {
		DataSourcePropertiesConfiguration dataSourcePropertiesConfiguration = new DataSourcePropertiesConfiguration();
		assertEquals("DataSourcePropertiesConfiguration valid field size was wrong", 0,
				dataSourcePropertiesConfiguration.getValidField().size());
		assertNull("DataSourcePropertiesConfiguration valid properties was not null",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());

		FileDataSourceProperties fileDataSourceProperties = buildFileProperties();
		NacosDataSourceProperties nacosDataSourceProperties = buildNacosProperties();

		dataSourcePropertiesConfiguration.setFile(fileDataSourceProperties);
		dataSourcePropertiesConfiguration.setNacos(nacosDataSourceProperties);

		assertEquals(
				"DataSourcePropertiesConfiguration valid field size was wrong after set file and nacos attribute",
				2, dataSourcePropertiesConfiguration.getValidField().size());
		assertNull(
				"DataSourcePropertiesConfiguration valid properties was not null after set file and nacos attribute",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());
	}

	@Test
	public void testFileConstructor() {
		DataSourcePropertiesConfiguration dataSourcePropertiesConfiguration = new DataSourcePropertiesConfiguration(
				buildFileProperties());
		assertEquals(
				"DataSourcePropertiesConfiguration file constructor valid field size was wrong",
				1, dataSourcePropertiesConfiguration.getValidField().size());
		assertNotNull(
				"DataSourcePropertiesConfiguration file constructor valid properties was null",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());
	}

	@Test
	public void testNacosConstructor() {
		DataSourcePropertiesConfiguration dataSourcePropertiesConfiguration = new DataSourcePropertiesConfiguration(
				buildNacosProperties());
		assertEquals(
				"DataSourcePropertiesConfiguration nacos constructor valid field size was wrong",
				1, dataSourcePropertiesConfiguration.getValidField().size());
		assertNotNull(
				"DataSourcePropertiesConfiguration nacos constructor valid properties was null",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());
	}

	@Test
	public void testApolloConstructor() {
		DataSourcePropertiesConfiguration dataSourcePropertiesConfiguration = new DataSourcePropertiesConfiguration(
				buildApolloProperties());
		assertEquals(
				"DataSourcePropertiesConfiguration apollo constructor valid field size was wrong",
				1, dataSourcePropertiesConfiguration.getValidField().size());
		assertNotNull(
				"DataSourcePropertiesConfiguration apollo constructor valid properties was null",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());
	}

	@Test
	public void testZKConstructor() {
		DataSourcePropertiesConfiguration dataSourcePropertiesConfiguration = new DataSourcePropertiesConfiguration(
				buildZKProperties());
		assertEquals(
				"DataSourcePropertiesConfiguration zk constructor valid field size was wrong",
				1, dataSourcePropertiesConfiguration.getValidField().size());
		assertNotNull(
				"DataSourcePropertiesConfiguration zk constructor valid properties was null",
				dataSourcePropertiesConfiguration.getValidDataSourceProperties());
	}

	private FileDataSourceProperties buildFileProperties() {
		FileDataSourceProperties fileDataSourceProperties = new FileDataSourceProperties();

		fileDataSourceProperties.setFile("/tmp/test.json");
		fileDataSourceProperties.setBufSize(1024);
		fileDataSourceProperties.setRecommendRefreshMs(2000);
		return fileDataSourceProperties;
	}

	private NacosDataSourceProperties buildNacosProperties() {
		NacosDataSourceProperties nacosDataSourceProperties = new NacosDataSourceProperties();
		nacosDataSourceProperties.setServerAddr("127.0.0.1:8848");
		nacosDataSourceProperties.setDataId("sentinel");
		nacosDataSourceProperties.setGroupId("custom-group");
		return nacosDataSourceProperties;
	}

	private ApolloDataSourceProperties buildApolloProperties() {
		ApolloDataSourceProperties apolloDataSourceProperties = new ApolloDataSourceProperties();
		apolloDataSourceProperties.setFlowRulesKey("test-key");
		apolloDataSourceProperties.setDefaultFlowRuleValue("dft-val");
		apolloDataSourceProperties.setNamespaceName("namespace");
		return apolloDataSourceProperties;
	}

	private ZookeeperDataSourceProperties buildZKProperties() {
		ZookeeperDataSourceProperties zookeeperDataSourceProperties = new ZookeeperDataSourceProperties();

		zookeeperDataSourceProperties.setServerAddr("localhost:2181");
		zookeeperDataSourceProperties.setPath("/path");
		return zookeeperDataSourceProperties;
	}

}
