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

package org.springframework.cloud.alibaba.sentinel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.alibaba.sentinel.custom.SentinelAutoConfiguration;
import org.springframework.cloud.alibaba.sentinel.datasource.RuleType;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
		"spring.cloud.sentinel.datasource.ds5.file.converter-class=org.springframework.cloud.alibaba.sentinel.TestConverter",
		"spring.cloud.sentinel.datasource.ds5.file.rule-type=param-flow" })
public class SentinelDataSourceTests {

	@Autowired
	private SentinelProperties sentinelProperties;

	@Test
	public void contextLoads() throws Exception {
		assertNotNull("SentinelProperties was not created", sentinelProperties);

		checkUrlPattern();
	}

	private void checkUrlPattern() {
		assertEquals("SentinelProperties filter order was wrong", Integer.MIN_VALUE,
				sentinelProperties.getFilter().getOrder());
		assertEquals("SentinelProperties filter url pattern size was wrong", 1,
				sentinelProperties.getFilter().getUrlPatterns().size());
		assertEquals("SentinelProperties filter url pattern was wrong", "/*",
				sentinelProperties.getFilter().getUrlPatterns().get(0));
	}

	@Test
	public void testDataSource() {
		assertEquals("DataSource size was wrong", 5,
				sentinelProperties.getDatasource().size());
		assertNull("DataSource ds1 apollo is not null",
				sentinelProperties.getDatasource().get("ds1").getApollo());
		assertNull("DataSource ds1 nacos is not null",
				sentinelProperties.getDatasource().get("ds1").getNacos());
		assertNull("DataSource ds1 zk is not null",
				sentinelProperties.getDatasource().get("ds1").getZk());
		assertNotNull("DataSource ds1 file is null",
				sentinelProperties.getDatasource().get("ds1").getFile());

		assertEquals("DataSource ds1 file dataType was wrong", "json",
				sentinelProperties.getDatasource().get("ds1").getFile().getDataType());
		assertEquals("DataSource ds1 file ruleType was wrong", RuleType.FLOW,
				sentinelProperties.getDatasource().get("ds1").getFile().getRuleType());

	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ SentinelAutoConfiguration.class,
			SentinelWebAutoConfiguration.class })
	public static class TestConfig {

	}

}
