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
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.cloud.alibaba.sentinel.datasource.config.NacosDataSourceProperties;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class NacosDataSourcePropertiesWithSystemPropertiesTests {

	@Test
	public void testNacosWithSystemProperties() {
		setSystemProperties();

		NacosDataSourceProperties nacosDataSourceProperties = new NacosDataSourceProperties();
		nacosDataSourceProperties.setServerAddr("127.0.0.1:8848");
		nacosDataSourceProperties.setGroupId("custom-group");
		nacosDataSourceProperties.setDataId("sentinel");
		nacosDataSourceProperties.preCheck("test-ds");

		assertEquals("Nacos groupId was wrong", "custom-group",
				nacosDataSourceProperties.getGroupId());
		assertEquals("Nacos dataId was wrong", "sentinel",
				nacosDataSourceProperties.getDataId());
		assertEquals("Nacos default data type was wrong", "json",
				nacosDataSourceProperties.getDataType());
		assertEquals("Nacos ak was wrong", "ak",
				nacosDataSourceProperties.getAccessKey());
		assertEquals("Nacos sk was wrong", "sk",
				nacosDataSourceProperties.getSecretKey());
		assertEquals("Nacos endpoint was wrong", "endpoint",
				nacosDataSourceProperties.getEndpoint());
		assertEquals("Nacos namespace was wrong", "namespace",
				nacosDataSourceProperties.getNamespace());
		assertNull("Nacos serverAddr was not null",
				nacosDataSourceProperties.getServerAddr());

	}

	@Test
	public void testNacosWithEDASAndSystemProperties() {
		setSystemProperties();

		NacosDataSourceProperties nacosDataSourceProperties = NacosDataSourceProperties
				.buildByEDAS(RuleType.FLOW.getName());

		assertEquals("Nacos groupId was wrong", "nacos-sentinel",
				nacosDataSourceProperties.getGroupId());
		assertEquals("Nacos dataId was wrong", "project-name-" + RuleType.FLOW.getName(),
				nacosDataSourceProperties.getDataId());
		assertEquals("Nacos default data type was wrong", "json",
				nacosDataSourceProperties.getDataType());
		assertEquals("Nacos rule type was wrong", RuleType.FLOW,
				nacosDataSourceProperties.getRuleType());
		assertEquals("Nacos ak was wrong", "ak",
				nacosDataSourceProperties.getAccessKey());
		assertEquals("Nacos sk was wrong", "sk",
				nacosDataSourceProperties.getSecretKey());
		assertEquals("Nacos endpoint was wrong", "endpoint",
				nacosDataSourceProperties.getEndpoint());
		assertEquals("Nacos namespace was wrong", "namespace",
				nacosDataSourceProperties.getNamespace());
		assertNull("Nacos serverAddr was not null",
				nacosDataSourceProperties.getServerAddr());

	}

	@Test
	public void testNacosWithEDASDegradeAndSystemProperties() {
		setSystemProperties();

		NacosDataSourceProperties nacosDataSourceProperties = NacosDataSourceProperties
				.buildDegradeByEDAS();
		assertEquals("Nacos groupId was wrong", "nacos-sentinel",
				nacosDataSourceProperties.getGroupId());
		assertEquals("Nacos dataId was wrong",
				"project-name-" + RuleType.DEGRADE.getName(),
				nacosDataSourceProperties.getDataId());
		assertEquals("Nacos default data type was wrong", "json",
				nacosDataSourceProperties.getDataType());
		assertEquals("Nacos rule type was wrong", RuleType.DEGRADE,
				nacosDataSourceProperties.getRuleType());
		assertEquals("Nacos ak was wrong", "ak",
				nacosDataSourceProperties.getAccessKey());
		assertEquals("Nacos sk was wrong", "sk",
				nacosDataSourceProperties.getSecretKey());
		assertEquals("Nacos endpoint was wrong", "endpoint",
				nacosDataSourceProperties.getEndpoint());
		assertEquals("Nacos namespace was wrong", "namespace",
				nacosDataSourceProperties.getNamespace());
		assertNull("Nacos serverAddr was not null",
				nacosDataSourceProperties.getServerAddr());

	}

	@Test
	public void testNacosWithEDASFlowAndSystemProperties() {
		setSystemProperties();

		NacosDataSourceProperties nacosDataSourceProperties = NacosDataSourceProperties
				.buildFlowByEDAS();
		assertEquals("Nacos groupId was wrong", "nacos-sentinel",
				nacosDataSourceProperties.getGroupId());
		assertEquals("Nacos dataId was wrong", "project-name-" + RuleType.FLOW.getName(),
				nacosDataSourceProperties.getDataId());
		assertEquals("Nacos default data type was wrong", "json",
				nacosDataSourceProperties.getDataType());
		assertEquals("Nacos rule type was wrong", RuleType.FLOW,
				nacosDataSourceProperties.getRuleType());
		assertEquals("Nacos ak was wrong", "ak",
				nacosDataSourceProperties.getAccessKey());
		assertEquals("Nacos sk was wrong", "sk",
				nacosDataSourceProperties.getSecretKey());
		assertEquals("Nacos endpoint was wrong", "endpoint",
				nacosDataSourceProperties.getEndpoint());
		assertEquals("Nacos namespace was wrong", "namespace",
				nacosDataSourceProperties.getNamespace());
		assertNull("Nacos serverAddr was not null",
				nacosDataSourceProperties.getServerAddr());
	}

	private void setSystemProperties() {
		System.setProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_ENDPOINT,
				"endpoint");
		System.setProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_NAMESPACE,
				"namespace");
		System.setProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_AK, "ak");
		System.setProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_SK, "sk");
		System.setProperty(SentinelDataSourceConstants.PROJECT_NAME, "project-name");
	}

}
