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

import java.util.Arrays;
import java.util.Optional;

import com.alibaba.cloud.sentinel.datasource.config.AbstractDataSourceProperties;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

import org.springframework.util.StringUtils;

/**
 * Enum for {@link AbstractRule} class, using in
 * {@link AbstractDataSourceProperties#ruleType}.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public enum RuleType {

	/**
	 * flow.
	 */
	FLOW("flow", FlowRule.class),
	/**
	 * degrade.
	 */
	DEGRADE("degrade", DegradeRule.class),
	/**
	 * param flow.
	 */
	PARAM_FLOW("param-flow", ParamFlowRule.class),
	/**
	 * system.
	 */
	SYSTEM("system", SystemRule.class),
	/**
	 * authority.
	 */
	AUTHORITY("authority", AuthorityRule.class),
	/**
	 * gateway flow.
	 */
	GW_FLOW("gw-flow",
			"com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule"),
	/**
	 * api.
	 */
	GW_API_GROUP("gw-api-group",
			"com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition");

	/**
	 * alias for {@link AbstractRule}.
	 */
	private final String name;

	/**
	 * concrete {@link AbstractRule} class.
	 */
	private Class clazz;

	/**
	 * concrete {@link AbstractRule} class name.
	 */
	private String clazzName;

	RuleType(String name, Class clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	RuleType(String name, String clazzName) {
		this.name = name;
		this.clazzName = clazzName;
	}

	public String getName() {
		return name;
	}

	public Class getClazz() {
		if (clazz != null) {
			return clazz;
		}
		else {
			try {
				return Class.forName(clazzName);
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static Optional<RuleType> getByName(String name) {
		if (StringUtils.isEmpty(name)) {
			return Optional.empty();
		}
		return Arrays.stream(RuleType.values())
				.filter(ruleType -> name.equals(ruleType.getName())).findFirst();
	}

	public static Optional<RuleType> getByClass(Class clazz) {
		return Arrays.stream(RuleType.values())
				.filter(ruleType -> clazz == ruleType.getClazz()).findFirst();
	}

}
