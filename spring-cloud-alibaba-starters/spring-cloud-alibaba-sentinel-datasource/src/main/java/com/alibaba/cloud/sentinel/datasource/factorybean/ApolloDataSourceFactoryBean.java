/*
 * Copyright 2013-present the original author or authors.
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

package com.alibaba.cloud.sentinel.datasource.factorybean;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} for creating {@link ApolloDataSource} instance.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see ApolloDataSource
 */
public class ApolloDataSourceFactoryBean implements FactoryBean<ApolloDataSource> {

	private @Nullable String namespaceName;

	private @Nullable String flowRulesKey;

	private @Nullable String defaultFlowRuleValue;

	private @Nullable Converter converter;

	@Override
	public ApolloDataSource getObject() throws Exception {
		if (namespaceName == null || flowRulesKey == null || defaultFlowRuleValue == null) {
			throw new IllegalStateException("namespaceName, flowRulesKey, and defaultFlowRuleValue must not be null");
		}
		return new ApolloDataSource(namespaceName, flowRulesKey, defaultFlowRuleValue,
				converter);
	}

	@Override
	public Class<?> getObjectType() {
		return ApolloDataSource.class;
	}

	public @Nullable String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(@Nullable String namespaceName) {
		this.namespaceName = namespaceName;
	}

	public @Nullable String getFlowRulesKey() {
		return flowRulesKey;
	}

	public void setFlowRulesKey(@Nullable String flowRulesKey) {
		this.flowRulesKey = flowRulesKey;
	}

	public @Nullable String getDefaultFlowRuleValue() {
		return defaultFlowRuleValue;
	}

	public void setDefaultFlowRuleValue(@Nullable String defaultFlowRuleValue) {
		this.defaultFlowRuleValue = defaultFlowRuleValue;
	}

	public @Nullable Converter getConverter() {
		return converter;
	}

	public void setConverter(@Nullable Converter converter) {
		this.converter = converter;
	}

}
