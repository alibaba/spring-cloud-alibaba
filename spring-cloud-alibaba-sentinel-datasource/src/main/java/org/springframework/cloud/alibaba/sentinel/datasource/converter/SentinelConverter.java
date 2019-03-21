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

package org.springframework.cloud.alibaba.sentinel.datasource.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.alibaba.sentinel.datasource.RuleType;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Convert sentinel rules for json or xml array Using strict mode to parse json or xml
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see FlowRule
 * @see DegradeRule
 * @see SystemRule
 * @see AuthorityRule
 * @see ParamFlowRule
 * @see ObjectMapper
 */
public abstract class SentinelConverter<T extends AbstractRule>
		implements Converter<String, List<AbstractRule>> {

	private static final Logger log = LoggerFactory.getLogger(SentinelConverter.class);

	private final ObjectMapper objectMapper;

	private final Class<T> ruleClass;

	public SentinelConverter(ObjectMapper objectMapper, Class<T> ruleClass) {
		this.objectMapper = objectMapper;
		this.ruleClass = ruleClass;
	}

	@Override
	public List<AbstractRule> convert(String source) {
		List<AbstractRule> ruleList = new ArrayList<>();
		if (StringUtils.isEmpty(source)) {
			log.warn("converter can not convert rules because source is empty");
			return ruleList;
		}
		try {
			List sourceArray = objectMapper.readValue(source,
					new TypeReference<List<HashMap>>() {
					});

			for (Object obj : sourceArray) {
				String item = null;
				try {
					item = objectMapper.writeValueAsString(obj);
				}
				catch (JsonProcessingException e) {
					// won't be happen
				}

				AbstractRule rule = convertRule(item);
				if (rule != null) {
					ruleList.add(rule);
				}

			}

			if (ruleList.size() != sourceArray.size()) {
				throw new IllegalArgumentException("convert " + ruleList.size()
						+ " rules but there are " + sourceArray.size()
						+ " rules from datasource. RuleClass: "
						+ ruleClass.getSimpleName());
			}
		}
		catch (Exception e) {
			throw new RuntimeException("convert error: " + e.getMessage(), e);
		}
		return ruleList;
	}

	private AbstractRule convertRule(String ruleStr) {
		try {
			final AbstractRule rule = objectMapper.readValue(ruleStr, ruleClass);
			RuleType ruleType = RuleType.getByClass(ruleClass);
			switch (ruleType) {
			case FLOW:
				if (!FlowRuleUtil.isValidRule((FlowRule) rule)) {
					return null;
				}
				break;
			case DEGRADE:
				if (!DegradeRuleManager.isValidRule((DegradeRule) rule)) {
					return null;
				}
			default:
				break;
			}
			return rule;
		}
		catch (Exception e) {
			// ignore
		}
		return null;
	}

}
