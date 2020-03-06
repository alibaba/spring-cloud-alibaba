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

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.datasource.factorybean.ApolloDataSourceFactoryBean;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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

		assertThat(factoryBean.getObject()).isEqualTo(apolloDataSource);
		assertThat(factoryBean.getObject().readSource()).isEqualTo("{}");
		assertThat(factoryBean.getConverter()).isEqualTo(converter);
		assertThat(factoryBean.getFlowRulesKey()).isEqualTo(flowRuleKey);
		assertThat(factoryBean.getNamespaceName()).isEqualTo(namespace);
		assertThat(factoryBean.getDefaultFlowRuleValue()).isEqualTo(defaultFlowValue);
	}

}
