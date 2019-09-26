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

import com.alibaba.cloud.sentinel.datasource.config.NacosDataSourceProperties;
import com.alibaba.cloud.sentinel.datasource.factorybean.NacosDataSourceFactoryBean;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

		assertThat(nacosDataSourceProperties.getGroupId()).isEqualTo("custom-group");
		assertThat(nacosDataSourceProperties.getDataId()).isEqualTo("sentinel");
		assertThat(nacosDataSourceProperties.getDataType()).isEqualTo("xml");
		assertThat(nacosDataSourceProperties.getRuleType()).isEqualTo(RuleType.FLOW);
		assertThat(nacosDataSourceProperties.getFactoryBeanName())
				.isEqualTo(NacosDataSourceFactoryBean.class.getName());
	}

	@Test
	public void testNacosWithProperties() {
		NacosDataSourceProperties nacosDataSourceProperties = new NacosDataSourceProperties();
		nacosDataSourceProperties.setAccessKey("ak");
		nacosDataSourceProperties.setSecretKey("sk");
		nacosDataSourceProperties.setEndpoint("endpoint");
		nacosDataSourceProperties.setNamespace("namespace");
		nacosDataSourceProperties.setRuleType(RuleType.SYSTEM);

		assertThat(nacosDataSourceProperties.getAccessKey()).isEqualTo("ak");
		assertThat(nacosDataSourceProperties.getSecretKey()).isEqualTo("sk");
		assertThat(nacosDataSourceProperties.getEndpoint()).isEqualTo("endpoint");
		assertThat(nacosDataSourceProperties.getNamespace()).isEqualTo("namespace");
		assertThat(nacosDataSourceProperties.getRuleType()).isEqualTo(RuleType.SYSTEM);
	}

}
