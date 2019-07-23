/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.sentinel.datasource;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.datasource.converter.XmlConverter;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class SentinelConverterTests {

	private ObjectMapper objectMapper = new ObjectMapper();

	private XmlMapper xmlMapper = new XmlMapper();

	@Test
	public void testJsonConverter() {
		JsonConverter jsonConverter = new JsonConverter(objectMapper, FlowRule.class);
		List<FlowRule> flowRules = (List<FlowRule>) jsonConverter
				.convert(readFileContent("classpath: flowrule.json"));
		assertEquals("json converter flow rule size was wrong", 1, flowRules.size());
		assertEquals("json converter flow rule resource name was wrong", "resource",
				flowRules.get(0).getResource());
		assertEquals("json converter flow rule limit app was wrong", "default",
				flowRules.get(0).getLimitApp());
		assertEquals("json converter flow rule count was wrong", "1.0",
				String.valueOf(flowRules.get(0).getCount()));
		assertEquals("json converter flow rule control behavior was wrong",
				RuleConstant.CONTROL_BEHAVIOR_DEFAULT,
				flowRules.get(0).getControlBehavior());
		assertEquals("json converter flow rule strategy was wrong",
				RuleConstant.STRATEGY_DIRECT, flowRules.get(0).getStrategy());
		assertEquals("json converter flow rule grade was wrong",
				RuleConstant.FLOW_GRADE_QPS, flowRules.get(0).getGrade());
	}

	@Test
	public void testConverterEmptyContent() {
		JsonConverter jsonConverter = new JsonConverter(objectMapper, FlowRule.class);
		List<FlowRule> flowRules = (List<FlowRule>) jsonConverter.convert("");
		assertEquals("json converter flow rule size was not empty", 0, flowRules.size());
	}

	@Test(expected = RuntimeException.class)
	public void testConverterErrorFormat() {
		JsonConverter jsonConverter = new JsonConverter(objectMapper, FlowRule.class);
		jsonConverter.convert(readFileContent("classpath: flowrule-errorformat.json"));
	}

	@Test(expected = RuntimeException.class)
	public void testConverterErrorContent() {
		JsonConverter jsonConverter = new JsonConverter(objectMapper, FlowRule.class);
		jsonConverter.convert(readFileContent("classpath: flowrule-errorcontent.json"));
	}

	@Test
	public void testXmlConverter() {
		XmlConverter jsonConverter = new XmlConverter(xmlMapper, FlowRule.class);
		List<FlowRule> flowRules = (List<FlowRule>) jsonConverter
				.convert(readFileContent("classpath: flowrule.xml"));
		assertEquals("xml converter flow rule size was wrong", 2, flowRules.size());
		assertEquals("xml converter flow rule1 resource name was wrong", "resource",
				flowRules.get(0).getResource());
		assertEquals("xml converter flow rule2 limit app was wrong", "default",
				flowRules.get(0).getLimitApp());
		assertEquals("xml converter flow rule1 count was wrong", "1.0",
				String.valueOf(flowRules.get(0).getCount()));
		assertEquals("xml converter flow rule1 control behavior was wrong",
				RuleConstant.CONTROL_BEHAVIOR_DEFAULT,
				flowRules.get(0).getControlBehavior());
		assertEquals("xml converter flow rule1 strategy was wrong",
				RuleConstant.STRATEGY_DIRECT, flowRules.get(0).getStrategy());
		assertEquals("xml converter flow rule1 grade was wrong",
				RuleConstant.FLOW_GRADE_QPS, flowRules.get(0).getGrade());

		assertEquals("xml converter flow rule2 resource name was wrong", "test",
				flowRules.get(1).getResource());
		assertEquals("xml converter flow rule2 limit app was wrong", "default",
				flowRules.get(1).getLimitApp());
		assertEquals("xml converter flow rule2 count was wrong", "1.0",
				String.valueOf(flowRules.get(1).getCount()));
		assertEquals("xml converter flow rule2 control behavior was wrong",
				RuleConstant.CONTROL_BEHAVIOR_DEFAULT,
				flowRules.get(1).getControlBehavior());
		assertEquals("xml converter flow rule2 strategy was wrong",
				RuleConstant.STRATEGY_DIRECT, flowRules.get(1).getStrategy());
		assertEquals("xml converter flow rule2 grade was wrong",
				RuleConstant.FLOW_GRADE_QPS, flowRules.get(1).getGrade());
	}

	private String readFileContent(String file) {
		try {
			return FileUtils.readFileToString(
					ResourceUtils.getFile(StringUtils.trimAllWhitespace(file)));
		}
		catch (IOException e) {
			return "";
		}
	}

}
