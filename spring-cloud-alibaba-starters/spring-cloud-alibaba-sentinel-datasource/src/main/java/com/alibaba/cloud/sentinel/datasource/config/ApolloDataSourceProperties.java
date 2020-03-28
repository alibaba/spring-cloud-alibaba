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

package com.alibaba.cloud.sentinel.datasource.config;

import javax.validation.constraints.NotEmpty;

import com.alibaba.cloud.sentinel.datasource.factorybean.ApolloDataSourceFactoryBean;

/**
 * Apollo Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link ApolloDataSourceFactoryBean}.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ApolloDataSourceProperties extends AbstractDataSourceProperties {

	@NotEmpty
	private String namespaceName;

	@NotEmpty
	private String flowRulesKey;

	private String defaultFlowRuleValue;

	public ApolloDataSourceProperties() {
		super(ApolloDataSourceFactoryBean.class.getName());
	}

	public String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

	public String getFlowRulesKey() {
		return flowRulesKey;
	}

	public void setFlowRulesKey(String flowRulesKey) {
		this.flowRulesKey = flowRulesKey;
	}

	public String getDefaultFlowRuleValue() {
		return defaultFlowRuleValue;
	}

	public void setDefaultFlowRuleValue(String defaultFlowRuleValue) {
		this.defaultFlowRuleValue = defaultFlowRuleValue;
	}

}
