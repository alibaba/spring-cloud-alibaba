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
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.alibaba.cloud.sentinel.datasource.config.ApolloDataSourceProperties;
import com.alibaba.cloud.sentinel.datasource.config.FileDataSourceProperties;
import com.alibaba.cloud.sentinel.datasource.config.ZookeeperDataSourceProperties;
import com.alibaba.cloud.sentinel.datasource.factorybean.ApolloDataSourceFactoryBean;
import com.alibaba.cloud.sentinel.datasource.factorybean.FileRefreshableDataSourceFactoryBean;
import com.alibaba.cloud.sentinel.datasource.factorybean.ZookeeperDataSourceFactoryBean;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class DataSourcePropertiesTests {

	@Test
	public void testApollo() {
		ApolloDataSourceProperties apolloDataSourceProperties = new ApolloDataSourceProperties();
		apolloDataSourceProperties.setFlowRulesKey("test-key");
		apolloDataSourceProperties.setDefaultFlowRuleValue("dft-val");
		apolloDataSourceProperties.setNamespaceName("namespace");
		apolloDataSourceProperties.setRuleType(RuleType.DEGRADE);
		assertEquals("Apollo flow rule key was wrong", "test-key",
				apolloDataSourceProperties.getFlowRulesKey());
		assertEquals("Apollo namespace was wrong", "namespace",
				apolloDataSourceProperties.getNamespaceName());
		assertEquals("Apollo default data type was wrong", "json",
				apolloDataSourceProperties.getDataType());
		Assert.assertEquals("Apollo rule type was wrong", RuleType.DEGRADE,
				apolloDataSourceProperties.getRuleType());
		assertEquals("Apollo default flow value was wrong", "dft-val",
				apolloDataSourceProperties.getDefaultFlowRuleValue());
		assertEquals("Apollo factory bean was wrong",
				ApolloDataSourceFactoryBean.class.getName(),
				apolloDataSourceProperties.getFactoryBeanName());
		assertNull("Apollo converterClass was not null",
				apolloDataSourceProperties.getConverterClass());
	}

	@Test
	public void testZK() {
		ZookeeperDataSourceProperties zookeeperDataSourceProperties = new ZookeeperDataSourceProperties();

		zookeeperDataSourceProperties.setServerAddr("localhost:2181");
		zookeeperDataSourceProperties.setGroupId("groupId");
		zookeeperDataSourceProperties.setDataId("dataId");
		zookeeperDataSourceProperties.setPath("/path");
		zookeeperDataSourceProperties.setConverterClass("test.ConverterClass");
		zookeeperDataSourceProperties.setRuleType(RuleType.AUTHORITY);

		assertEquals("ZK serverAddr was wrong", "localhost:2181",
				zookeeperDataSourceProperties.getServerAddr());
		assertEquals("ZK groupId was wrong", "groupId",
				zookeeperDataSourceProperties.getGroupId());
		assertEquals("ZK dataId was wrong", "dataId",
				zookeeperDataSourceProperties.getDataId());
		assertEquals("ZK path was wrong", "/path",
				zookeeperDataSourceProperties.getPath());
		assertEquals("ZK factory bean was wrong",
				ZookeeperDataSourceFactoryBean.class.getName(),
				zookeeperDataSourceProperties.getFactoryBeanName());
		assertEquals("ZK custom converter class was wrong", "test.ConverterClass",
				zookeeperDataSourceProperties.getConverterClass());
		Assert.assertEquals("ZK rule type was wrong", RuleType.AUTHORITY,
				zookeeperDataSourceProperties.getRuleType());
	}

	@Test
	public void testFileDefaultValue() {
		FileDataSourceProperties fileDataSourceProperties = new FileDataSourceProperties();

		fileDataSourceProperties.setFile("/tmp/test.json");
		fileDataSourceProperties.setRuleType(RuleType.PARAM_FLOW);

		assertEquals("File path was wrong", "/tmp/test.json",
				fileDataSourceProperties.getFile());
		assertEquals("File charset was wrong", "utf-8",
				fileDataSourceProperties.getCharset());
		assertEquals("File refresh time was wrong", 3000L,
				fileDataSourceProperties.getRecommendRefreshMs());
		assertEquals("File buf size was wrong", 1024 * 1024,
				fileDataSourceProperties.getBufSize());
		assertEquals("File factory bean was wrong",
				FileRefreshableDataSourceFactoryBean.class.getName(),
				fileDataSourceProperties.getFactoryBeanName());
		Assert.assertEquals("File rule type was wrong", RuleType.PARAM_FLOW,
				fileDataSourceProperties.getRuleType());
	}

	@Test
	public void testFileCustomValue() {
		FileDataSourceProperties fileDataSourceProperties = new FileDataSourceProperties();

		fileDataSourceProperties.setFile("/tmp/test.json");
		fileDataSourceProperties.setBufSize(1024);
		fileDataSourceProperties.setRecommendRefreshMs(2000);
		fileDataSourceProperties.setCharset("ISO8859-1");

		assertEquals("File path was wrong", "/tmp/test.json",
				fileDataSourceProperties.getFile());
		assertEquals("File charset was wrong", "ISO8859-1",
				fileDataSourceProperties.getCharset());
		assertEquals("File refresh time was wrong", 2000L,
				fileDataSourceProperties.getRecommendRefreshMs());
		assertEquals("File buf size was wrong", 1024,
				fileDataSourceProperties.getBufSize());
	}

	@Test(expected = RuntimeException.class)
	public void testFileException() {
		FileDataSourceProperties fileDataSourceProperties = new FileDataSourceProperties();
		fileDataSourceProperties.setFile("classpath: 1.json");
		fileDataSourceProperties.preCheck("test-ds");
	}

	@Test
	public void testPostRegister() throws Exception {
		FileDataSourceProperties fileDataSourceProperties = new FileDataSourceProperties();

		fileDataSourceProperties.setFile("classpath: flowrule.json");
		fileDataSourceProperties.setRuleType(RuleType.FLOW);

		FileRefreshableDataSource fileRefreshableDataSource = new FileRefreshableDataSource(
				ResourceUtils
						.getFile(StringUtils
								.trimAllWhitespace(fileDataSourceProperties.getFile()))
						.getAbsolutePath(),
				new Converter<String, List<FlowRule>>() {
					ObjectMapper objectMapper = new ObjectMapper();

					@Override
					public List<FlowRule> convert(String source) {
						try {
							return objectMapper.readValue(source,
									new TypeReference<List<FlowRule>>() {
									});
						}
						catch (IOException e) {
							// ignore
						}
						return null;
					}
				});
		fileDataSourceProperties.postRegister(fileRefreshableDataSource);
		assertEquals("DataSourceProperties postRegister error",
				fileRefreshableDataSource.loadConfig(), FlowRuleManager.getRules());
	}

}
