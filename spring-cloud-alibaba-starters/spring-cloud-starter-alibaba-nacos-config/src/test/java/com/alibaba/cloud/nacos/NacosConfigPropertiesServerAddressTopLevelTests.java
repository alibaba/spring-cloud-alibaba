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

package com.alibaba.cloud.nacos;

import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpointAutoConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.nacos.NacosConfigPropertiesServerAddressTopLevelTests.TestConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * @author <a href="mailto:lyuzb@lyuzb.com">lyuzb</a>
 * @author freeman
 */
@SpringBootTest(classes = TestConfig.class, webEnvironment = NONE, properties = {
		"spring.cloud.nacos.server-addr=123.123.123.123:8848",
		"spring.cloud.bootstrap.enabled=true" })
public class NacosConfigPropertiesServerAddressTopLevelTests {

	@Autowired
	private NacosConfigProperties properties;

	@Test
	public void testGetServerAddr() {
		assertThat(properties.getServerAddr()).isEqualTo("123.123.123.123:8848");
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ NacosConfigEndpointAutoConfiguration.class,
			NacosConfigAutoConfiguration.class, NacosConfigBootstrapConfiguration.class })
	public static class TestConfig {

	}

}
