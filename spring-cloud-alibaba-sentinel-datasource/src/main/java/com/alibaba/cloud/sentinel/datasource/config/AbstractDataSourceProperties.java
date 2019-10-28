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
import javax.validation.constraints.NotNull;

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

import org.springframework.core.env.Environment;

/**
 * Abstract class Using by {@link DataSourcePropertiesConfiguration}.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class AbstractDataSourceProperties {

	@NotEmpty
	private String dataType = "json";

	@NotNull
	private RuleType ruleType;

	private String converterClass;

	@JsonIgnore
	private final String factoryBeanName;

	@JsonIgnore
	private Environment env;

	public AbstractDataSourceProperties(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public RuleType getRuleType() {
		return ruleType;
	}

	public void setRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
	}

	public String getConverterClass() {
		return converterClass;
	}

	public void setConverterClass(String converterClass) {
		this.converterClass = converterClass;
	}

	public String getFactoryBeanName() {
		return factoryBeanName;
	}

	protected Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}

	public void preCheck(String dataSourceName) {

	}

	public void postRegister(AbstractDataSource dataSource) {
		switch (this.getRuleType()) {
		case FLOW:
			FlowRuleManager.register2Property(dataSource.getProperty());
			break;
		case DEGRADE:
			DegradeRuleManager.register2Property(dataSource.getProperty());
			break;
		case PARAM_FLOW:
			ParamFlowRuleManager.register2Property(dataSource.getProperty());
			break;
		case SYSTEM:
			SystemRuleManager.register2Property(dataSource.getProperty());
			break;
		case AUTHORITY:
			AuthorityRuleManager.register2Property(dataSource.getProperty());
			break;
		case GW_FLOW:
			GatewayRuleManager.register2Property(dataSource.getProperty());
			break;
		case GW_API_GROUP:
			GatewayApiDefinitionManager.register2Property(dataSource.getProperty());
			break;
		default:
			break;
		}
	}

}
