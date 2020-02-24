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

package com.alibaba.cloud.sentinel;

import com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration;
import com.alibaba.cloud.sentinel.datasource.RuleType;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SentinelDataSourceTests.TestConfig.class }, properties = {
		"spring.cloud.sentinel.datasource.ds1.file.file=classpath: flowrule.json",
		"spring.cloud.sentinel.datasource.ds1.file.data-type=json",
		"spring.cloud.sentinel.datasource.ds1.file.rule-type=flow",

		"spring.cloud.sentinel.datasource.ds2.file.file=classpath: degraderule.json",
		"spring.cloud.sentinel.datasource.ds2.file.data-type=json",
		"spring.cloud.sentinel.datasource.ds2.file.rule-type=degrade",

		"spring.cloud.sentinel.datasource.ds3.file.file=classpath: authority.json",
		"spring.cloud.sentinel.datasource.ds3.file.rule-type=authority",

		"spring.cloud.sentinel.datasource.ds4.file.file=classpath: system.json",
		"spring.cloud.sentinel.datasource.ds4.file.rule-type=system",

		"spring.cloud.sentinel.datasource.ds5.file.file=classpath: param-flow.json",
		"spring.cloud.sentinel.datasource.ds5.file.data-type=custom",
		"spring.cloud.sentinel.datasource.ds5.file.converter-class=TestConverter",
		"spring.cloud.sentinel.datasource.ds5.file.rule-type=param-flow" })
public class SentinelDataSourceTests {

	@Autowired
	private SentinelProperties sentinelProperties;

	@Test
	public void contextLoads() throws Exception {
		assertThat(sentinelProperties).isNotNull();

		checkUrlPattern();
	}

	private void checkUrlPattern() {
		assertThat(sentinelProperties.getFilter().getOrder())
				.isEqualTo(Integer.MIN_VALUE);
		assertThat(sentinelProperties.getFilter().getUrlPatterns().size()).isEqualTo(1);
		assertThat(sentinelProperties.getFilter().getUrlPatterns().get(0))
				.isEqualTo("/**");
	}

	@Test
	public void testDataSource() {
		assertThat(sentinelProperties.getDatasource().size()).isEqualTo(5);
		assertThat(sentinelProperties.getDatasource().get("ds1").getApollo()).isNull();
		assertThat(sentinelProperties.getDatasource().get("ds1").getNacos()).isNull();
		assertThat(sentinelProperties.getDatasource().get("ds1").getZk()).isNull();
		assertThat(sentinelProperties.getDatasource().get("ds1").getFile()).isNotNull();

		assertThat(sentinelProperties.getDatasource().get("ds1").getFile().getDataType())
				.isEqualTo("json");
		assertThat(sentinelProperties.getDatasource().get("ds1").getFile().getRuleType())
				.isEqualTo(RuleType.FLOW);
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ SentinelAutoConfiguration.class,
			SentinelWebAutoConfiguration.class })
	public static class TestConfig {

	}

}
