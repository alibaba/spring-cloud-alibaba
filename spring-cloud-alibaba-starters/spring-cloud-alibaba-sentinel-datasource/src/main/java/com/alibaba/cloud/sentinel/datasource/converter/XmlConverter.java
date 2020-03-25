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

package com.alibaba.cloud.sentinel.datasource.converter;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Convert sentinel rules for xml array Using strict mode to parse xml.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see FlowRule
 * @see DegradeRule
 * @see SystemRule
 * @see AuthorityRule
 * @see ParamFlowRule
 * @see ObjectMapper
 */
public class XmlConverter<T> extends SentinelConverter {

	public XmlConverter(XmlMapper xmlMapper, Class<T> ruleClass) {
		super(xmlMapper, ruleClass);
	}

}
