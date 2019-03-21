/*
 * Copyright (C) 2018 the original author or authors.
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

package org.springframework.cloud.alibaba.sentinel.datasource;

import org.springframework.cloud.alibaba.sentinel.datasource.config.AbstractDataSourceProperties;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * Enum for {@link AbstractRule} class, using in
 * {@link AbstractDataSourceProperties#ruleType}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public enum RuleType {

	/**
	 * flow
	 */
	FLOW("flow", FlowRule.class),
	/**
	 * degrade
	 */
	DEGRADE("degrade", DegradeRule.class),
	/**
	 * param flow
	 */
	PARAM_FLOW("param-flow", ParamFlowRule.class),
	/**
	 * system
	 */
	SYSTEM("system", SystemRule.class),
	/**
	 * authority
	 */
	AUTHORITY("authority", AuthorityRule.class);

	/**
	 * alias for {@link AbstractRule}
	 */
	private final String name;

	/**
	 * concrete {@link AbstractRule} class
	 */
	private final Class clazz;

	RuleType(String name, Class clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	public String getName() {
		return name;
	}

	public Class getClazz() {
		return clazz;
	}

	public static RuleType getByName(String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (RuleType ruleType : RuleType.values()) {
			if (name.equals(ruleType.getName())) {
				return ruleType;
			}
		}
		return null;
	}

	public static RuleType getByClass(Class clazz) {
		for (RuleType ruleType : RuleType.values()) {
			if (clazz.equals(ruleType.getClazz())) {
				return ruleType;
			}
		}
		return null;
	}

}