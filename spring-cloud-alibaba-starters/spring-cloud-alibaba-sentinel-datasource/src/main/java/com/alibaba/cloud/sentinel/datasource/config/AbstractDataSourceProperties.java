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


import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.Nullable;

import org.springframework.core.env.Environment;

/**
 * Abstract class Using by {@link DataSourcePropertiesConfiguration}.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class AbstractDataSourceProperties {

	private String dataType = "json";

	private @Nullable RuleType ruleType;

	private @Nullable String converterClass;

	@JsonIgnore
	private final String factoryBeanName;

	@JsonIgnore
	private @Nullable Environment env;

	public AbstractDataSourceProperties(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(@Nullable String dataType) {
		this.dataType = dataType == null ? "json" : dataType;
	}

	public @Nullable RuleType getRuleType() {
		return ruleType;
	}

	public void setRuleType(@Nullable RuleType ruleType) {
		this.ruleType = ruleType;
	}

	public @Nullable String getConverterClass() {
		return converterClass;
	}

	public void setConverterClass(@Nullable String converterClass) {
		this.converterClass = converterClass;
	}

	public String getFactoryBeanName() {
		return factoryBeanName;
	}

	protected @Nullable Environment getEnv() {
		return env;
	}

	public void setEnv(@Nullable Environment env) {
		this.env = env;
	}

	public void preCheck(String dataSourceName) {

	}

	public void postRegister(AbstractDataSource dataSource) {
		RuleType ruleType = this.getRuleType();
		if (ruleType == null) {
			return;
		}
		switch (ruleType) {
		case FLOW -> FlowRuleManager.register2Property(dataSource.getProperty());
		case DEGRADE -> DegradeRuleManager.register2Property(dataSource.getProperty());
		case PARAM_FLOW -> ParamFlowRuleManager.register2Property(dataSource.getProperty());
		case SYSTEM -> SystemRuleManager.register2Property(dataSource.getProperty());
		case AUTHORITY -> AuthorityRuleManager.register2Property(dataSource.getProperty());
		case GW_FLOW -> GatewayRuleManager.register2Property(dataSource.getProperty());
		case GW_API_GROUP -> GatewayApiDefinitionManager.register2Property(dataSource.getProperty());
		}
	}

}
