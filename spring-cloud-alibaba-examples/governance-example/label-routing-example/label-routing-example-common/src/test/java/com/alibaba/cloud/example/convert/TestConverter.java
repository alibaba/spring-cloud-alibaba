/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.example.convert;

import java.io.IOException;
import java.util.List;

import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import com.alibaba.cloud.routing.consumer.config.ConsumerCommonConfig;
import com.alibaba.cloud.routing.consumer.converter.Converter;
import com.alibaba.cloud.routing.consumer.converter.JsonConverter;
import com.alibaba.cloud.routing.consumer.util.ReadJsonFileUtils;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@SpringBootTest(classes = TestConverter.TestConfig.class)
@RunWith(SpringRunner.class)
public class TestConverter {

	Resource resource = new ClassPathResource("add-routing-rule.json");

	@Autowired
	private Converter<String, List<UnifiedRoutingDataStructure>> jsonConverter;

	private static final String data = "[{\"targetService\":\"routing-service-provider\",\"labelRouteRule\":{\"matchRouteList\":[{\"ruleList\":[{\"value\":\"v2\",\"key\":\"tag\",\"type\":\"header\",\"condition\":\"=\"},{\"value\":\"10\",\"key\":\"id\",\"type\":\"parameter\",\"condition\":\">\"},{\"value\":\"/router-test\",\"key\":null,\"type\":\"path\",\"condition\":\"=\"}],\"version\":\"v2\",\"weight\":100,\"fallback\":null}],\"defaultRouteVersion\":\"v1\"}}]";

	@Test
	public void test_json_converter() throws IOException {

		String content = ReadJsonFileUtils
				.convertFile2String(resource.getFile().getPath());

		Assert.assertEquals(data,
				new JsonMapper().writeValueAsString(jsonConverter.convert(content)));

	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ ConsumerCommonConfig.class })
	public static class TestConfig {

		@Bean
		public Converter<String, List<UnifiedRoutingDataStructure>> jonConverter() {

			return new JsonConverter();
		}

	}

}
