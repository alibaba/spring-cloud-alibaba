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

import java.io.IOException;
import java.util.List;

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.datasource.converter.XmlConverter;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

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

		assertThat(flowRules.size()).isEqualTo(1);
		assertThat(flowRules.get(0).getResource()).isEqualTo("resource");
		assertThat(flowRules.get(0).getLimitApp()).isEqualTo("default");
		assertThat(String.valueOf(flowRules.get(0).getCount())).isEqualTo("1.0");
		assertThat(flowRules.get(0).getControlBehavior())
				.isEqualTo(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
		assertThat(flowRules.get(0).getStrategy())
				.isEqualTo(RuleConstant.STRATEGY_DIRECT);
		assertThat(flowRules.get(0).getGrade()).isEqualTo(RuleConstant.FLOW_GRADE_QPS);
	}

	@Test
	public void testConverterEmptyContent() {
		JsonConverter jsonConverter = new JsonConverter(objectMapper, FlowRule.class);
		List<FlowRule> flowRules = (List<FlowRule>) jsonConverter.convert("");
		assertThat(flowRules.size()).isEqualTo(0);
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

		assertThat(flowRules.size()).isEqualTo(2);
		assertThat(flowRules.get(0).getResource()).isEqualTo("resource");
		assertThat(flowRules.get(0).getLimitApp()).isEqualTo("default");
		assertThat(String.valueOf(flowRules.get(0).getCount())).isEqualTo("1.0");
		assertThat(flowRules.get(0).getControlBehavior())
				.isEqualTo(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
		assertThat(flowRules.get(0).getStrategy())
				.isEqualTo(RuleConstant.STRATEGY_DIRECT);
		assertThat(flowRules.get(0).getGrade()).isEqualTo(RuleConstant.FLOW_GRADE_QPS);

		assertThat(flowRules.get(1).getResource()).isEqualTo("test");
		assertThat(flowRules.get(1).getLimitApp()).isEqualTo("default");
		assertThat(String.valueOf(flowRules.get(1).getCount())).isEqualTo("1.0");
		assertThat(flowRules.get(1).getControlBehavior())
				.isEqualTo(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
		assertThat(flowRules.get(1).getStrategy())
				.isEqualTo(RuleConstant.STRATEGY_DIRECT);
		assertThat(flowRules.get(1).getGrade()).isEqualTo(RuleConstant.FLOW_GRADE_QPS);
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
