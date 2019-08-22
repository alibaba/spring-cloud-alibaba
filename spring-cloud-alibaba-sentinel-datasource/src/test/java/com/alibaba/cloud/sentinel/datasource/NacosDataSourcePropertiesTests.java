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

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.cloud.sentinel.datasource.config.NacosDataSourceProperties;
import com.alibaba.cloud.sentinel.datasource.factorybean.NacosDataSourceFactoryBean;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class NacosDataSourcePropertiesTests {

	@Test
	public void testNacosWithAddr() {
		NacosDataSourceProperties nacosDataSourceProperties = new NacosDataSourceProperties();
		nacosDataSourceProperties.setServerAddr("127.0.0.1:8848");
		nacosDataSourceProperties.setRuleType(RuleType.FLOW);
		nacosDataSourceProperties.setDataId("sentinel");
		nacosDataSourceProperties.setGroupId("custom-group");
		nacosDataSourceProperties.setDataType("xml");

		assertEquals("Nacos groupId was wrong", "custom-group",
				nacosDataSourceProperties.getGroupId());
		assertEquals("Nacos dataId was wrong", "sentinel",
				nacosDataSourceProperties.getDataId());
		assertEquals("Nacos default data type was wrong", "xml",
				nacosDataSourceProperties.getDataType());
		Assert.assertEquals("Nacos rule type was wrong", RuleType.FLOW,
				nacosDataSourceProperties.getRuleType());
		assertEquals("Nacos default factory bean was wrong",
				NacosDataSourceFactoryBean.class.getName(),
				nacosDataSourceProperties.getFactoryBeanName());
	}

	@Test
	public void testNacosWithProperties() {
		NacosDataSourceProperties nacosDataSourceProperties = new NacosDataSourceProperties();
		nacosDataSourceProperties.setAccessKey("ak");
		nacosDataSourceProperties.setSecretKey("sk");
		nacosDataSourceProperties.setEndpoint("endpoint");
		nacosDataSourceProperties.setNamespace("namespace");
		nacosDataSourceProperties.setRuleType(RuleType.SYSTEM);

		assertEquals("Nacos ak was wrong", "ak",
				nacosDataSourceProperties.getAccessKey());
		assertEquals("Nacos sk was wrong", "sk",
				nacosDataSourceProperties.getSecretKey());
		assertEquals("Nacos endpoint was wrong", "endpoint",
				nacosDataSourceProperties.getEndpoint());
		assertEquals("Nacos namespace was wrong", "namespace",
				nacosDataSourceProperties.getNamespace());
		Assert.assertEquals("Nacos rule type was wrong", RuleType.SYSTEM,
				nacosDataSourceProperties.getRuleType());
	}

}
