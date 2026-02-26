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

package com.alibaba.cloud.sentinel.gateway;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import com.alibaba.cloud.commons.io.FileUtils;
import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.datasource.converter.XmlConverter;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SentinelGatewayAutoConfigurationTest {

	private SentinelGatewayAutoConfiguration.SentinelConverterConfiguration.SentinelJsonConfiguration json;
	private SentinelGatewayAutoConfiguration.SentinelConverterConfiguration.SentinelXmlConfiguration xml;

	/**
	 * Setup method to initialize test configurations.
	 */
	@BeforeEach
	public void setup() {
		json = new SentinelGatewayAutoConfiguration.SentinelConverterConfiguration.SentinelJsonConfiguration();
		xml = new SentinelGatewayAutoConfiguration.SentinelConverterConfiguration.SentinelXmlConfiguration();
	}

	/**
	 * Tests the JSON gateway flow rule converter.
	 * Reads JSON content from a file, converts it to GatewayFlowRule collection, and validates the result.
	 */
	@Test
	public void testJsonGatewayFlowConverter() {
		JsonConverter<GatewayFlowRule> jsonGatewayFlowConverter = json.jsonGatewayFlowConverter();
		Collection<GatewayFlowRule> gatewayFlowRules = jsonGatewayFlowConverter.convert(readFileContent("classpath: gatewayflowrule.json"));
		assertEquals(1, gatewayFlowRules.size());
		assertEquals("test", new ArrayList<>(gatewayFlowRules).get(0).getResource());
	}

	/**
	 * Tests the JSON API definition converter.
	 * Reads JSON content from a file, converts it to ApiDefinition collection, and validates the result.
	 */
	@Test
	public void testJsonApiConverter() {
		JsonConverter<ApiDefinition> jsonApiConverter = json.jsonApiConverter();
		Collection<ApiDefinition> apiDefinitions = jsonApiConverter.convert(readFileContent("classpath: apidefinition.json"));
		assertEquals(1, apiDefinitions.size());
		assertEquals("test", new ArrayList<>(apiDefinitions).get(0).getApiName());
	}

	/**
	 * Tests the XML gateway flow rule converter.
	 * Reads XML content from a file, converts it to GatewayFlowRule collection, and validates the result.
	 */
	@Test
	public void testXmlGatewayFlowConverter() {
		XmlConverter<GatewayFlowRule> xmlGatewayFlowConverter = xml.xmlGatewayFlowConverter();
		Collection<GatewayFlowRule> gatewayFlowRules = xmlGatewayFlowConverter.convert(readFileContent("classpath: gatewayflowrule.xml"));
		assertEquals(1, gatewayFlowRules.size());
		assertEquals("test", new ArrayList<>(gatewayFlowRules).get(0).getResource());
	}

	/**
	 * Tests the XML API definition converter.
	 * Reads XML content from a file, converts it to ApiDefinition collection, and validates the result.
	 */
	@Test
	public void testSentinelXmlConfiguration() {
		XmlConverter<ApiDefinition> xmlApiConverter = xml.xmlApiConverter();
		Collection<ApiDefinition> apiDefinitions = xmlApiConverter.convert(readFileContent("classpath: apidefinition.xml"));
		assertEquals(1, apiDefinitions.size());
		assertEquals("test", new ArrayList<>(apiDefinitions).get(0).getApiName());
	}

	/**
	 * Reads the content of a file.
	 *
	 * @param file the classpath location of the file
	 * @return the content of the file as a string
	 */
	private String readFileContent(String file) {
		try {
			return FileUtils.readFileToString(
					ResourceUtils.getFile(StringUtils.trimAllWhitespace(file)),
					Charset.defaultCharset());
		}
		catch (IOException e) {
			return "";
		}
	}
}
