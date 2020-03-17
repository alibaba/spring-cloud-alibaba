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

import java.io.IOException;
import java.util.List;

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
import org.junit.Test;

import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

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

		assertThat(apolloDataSourceProperties.getFlowRulesKey()).isEqualTo("test-key");
		assertThat(apolloDataSourceProperties.getNamespaceName()).isEqualTo("namespace");
		assertThat(apolloDataSourceProperties.getDataType()).isEqualTo("json");
		assertThat(apolloDataSourceProperties.getRuleType()).isEqualTo(RuleType.DEGRADE);
		assertThat(apolloDataSourceProperties.getDefaultFlowRuleValue())
				.isEqualTo("dft-val");
		assertThat(apolloDataSourceProperties.getFactoryBeanName())
				.isEqualTo(ApolloDataSourceFactoryBean.class.getName());
		assertThat(apolloDataSourceProperties.getConverterClass()).isNull();
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

		assertThat(zookeeperDataSourceProperties.getServerAddr())
				.isEqualTo("localhost:2181");
		assertThat(zookeeperDataSourceProperties.getGroupId()).isEqualTo("groupId");
		assertThat(zookeeperDataSourceProperties.getDataId()).isEqualTo("dataId");
		assertThat(zookeeperDataSourceProperties.getPath()).isEqualTo("/path");
		assertThat(zookeeperDataSourceProperties.getFactoryBeanName())
				.isEqualTo(ZookeeperDataSourceFactoryBean.class.getName());
		assertThat(zookeeperDataSourceProperties.getConverterClass())
				.isEqualTo("test.ConverterClass");
		assertThat(zookeeperDataSourceProperties.getRuleType())
				.isEqualTo(RuleType.AUTHORITY);
	}

	@Test
	public void testFileDefaultValue() {
		FileDataSourceProperties fileDataSourceProperties = new FileDataSourceProperties();

		fileDataSourceProperties.setFile("/tmp/test.json");
		fileDataSourceProperties.setRuleType(RuleType.PARAM_FLOW);

		assertThat(fileDataSourceProperties.getFile()).isEqualTo("/tmp/test.json");
		assertThat(fileDataSourceProperties.getCharset()).isEqualTo("utf-8");
		assertThat(fileDataSourceProperties.getRecommendRefreshMs()).isEqualTo(3000L);
		assertThat(fileDataSourceProperties.getBufSize()).isEqualTo(1024 * 1024);
		assertThat(fileDataSourceProperties.getFactoryBeanName())
				.isEqualTo(FileRefreshableDataSourceFactoryBean.class.getName());
		assertThat(fileDataSourceProperties.getRuleType()).isEqualTo(RuleType.PARAM_FLOW);
	}

	@Test
	public void testFileCustomValue() {
		FileDataSourceProperties fileDataSourceProperties = new FileDataSourceProperties();

		fileDataSourceProperties.setFile("/tmp/test.json");
		fileDataSourceProperties.setBufSize(1024);
		fileDataSourceProperties.setRecommendRefreshMs(2000);
		fileDataSourceProperties.setCharset("ISO8859-1");

		assertThat(fileDataSourceProperties.getFile()).isEqualTo("/tmp/test.json");
		assertThat(fileDataSourceProperties.getCharset()).isEqualTo("ISO8859-1");
		assertThat(fileDataSourceProperties.getRecommendRefreshMs()).isEqualTo(2000L);
		assertThat(fileDataSourceProperties.getBufSize()).isEqualTo(1024);
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
		assertThat(FlowRuleManager.getRules())
				.isEqualTo(fileRefreshableDataSource.loadConfig());
	}

}
