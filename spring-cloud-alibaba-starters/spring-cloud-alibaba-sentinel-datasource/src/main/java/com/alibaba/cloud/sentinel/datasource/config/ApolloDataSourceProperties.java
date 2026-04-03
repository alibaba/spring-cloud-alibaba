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

package com.alibaba.cloud.sentinel.datasource.config;


import com.alibaba.cloud.sentinel.datasource.factorybean.ApolloDataSourceFactoryBean;
import org.jspecify.annotations.Nullable;

/**
 * Apollo Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link ApolloDataSourceFactoryBean}.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ApolloDataSourceProperties extends AbstractDataSourceProperties {

	private @Nullable String namespaceName;

	private @Nullable String flowRulesKey;

	private @Nullable String defaultFlowRuleValue;

	public ApolloDataSourceProperties() {
		super(ApolloDataSourceFactoryBean.class.getName());
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

}
