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

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.datasource.factorybean.ApolloDataSourceFactoryBean;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ApolloDataSourceFactoryBeanTests {

	private String flowRuleKey = "sentinel";
	private String namespace = "namespace";
	private String defaultFlowValue = "{}";

	@Test
	public void testApolloFactoryBean() throws Exception {
		ApolloDataSourceFactoryBean factoryBean = spy(new ApolloDataSourceFactoryBean());

		Converter converter = mock(JsonConverter.class);

		factoryBean.setDefaultFlowRuleValue(defaultFlowValue);
		factoryBean.setFlowRulesKey(flowRuleKey);
		factoryBean.setNamespaceName(namespace);
		factoryBean.setConverter(converter);

		ApolloDataSource apolloDataSource = mock(ApolloDataSource.class);

		when(apolloDataSource.readSource()).thenReturn("{}");
		doReturn(apolloDataSource).when(factoryBean).getObject();

		assertEquals("ApolloDataSourceFactoryBean getObject error", apolloDataSource,
				factoryBean.getObject());
		assertEquals("ApolloDataSource read source value was wrong", "{}",
				factoryBean.getObject().readSource());
		assertEquals("ApolloDataSource converter was wrong", converter,
				factoryBean.getConverter());
		assertEquals("ApolloDataSourceFactoryBean flowRuleKey was wrong", flowRuleKey,
				factoryBean.getFlowRulesKey());
		assertEquals("ApolloDataSourceFactoryBean namespace was wrong", namespace,
				factoryBean.getNamespaceName());
		assertEquals("ApolloDataSourceFactoryBean defaultFlowValue was wrong",
				defaultFlowValue, factoryBean.getDefaultFlowRuleValue());
	}

}
