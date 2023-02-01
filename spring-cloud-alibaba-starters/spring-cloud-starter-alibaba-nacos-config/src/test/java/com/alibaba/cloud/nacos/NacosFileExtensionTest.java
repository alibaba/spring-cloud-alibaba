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
import com.alibaba.nacos.client.config.NacosConfigService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 *
 * @author xiaojing
 * @author freeman
 */
@SpringBootTest(classes = NacosFileExtensionTest.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.application.name=test-name",
		"spring.cloud.nacos.config.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.config.file-extension=yaml",
		"spring.cloud.bootstrap.enabled=true" })
public class NacosFileExtensionTest {

	static {

		try {
			NacosConfigService mockedNacosConfigService = Mockito
					.mock(NacosConfigService.class);
			when(mockedNacosConfigService.getConfig(any(), any(), anyLong()))
					.thenAnswer(new Answer<String>() {
						@Override
						public String answer(InvocationOnMock invocationOnMock)
								throws Throwable {
							String dataId = invocationOnMock.getArgument(0, String.class);
							String group = invocationOnMock.getArgument(1, String.class);
							if ("test-name.yaml".equals(dataId)
									&& "DEFAULT_GROUP".equals(group)) {
								return "user:\n  name: hello\n  age: 12\n---\nuser:\n  gender: male";
							}
							return "";
						}

					});

			ReflectionTestUtils.setField(NacosConfigManager.class, "service",
					mockedNacosConfigService);

		}
		catch (Exception ignore) {
			ignore.printStackTrace();

		}
	}

	@Autowired
	private Environment environment;

	@Test
	public void contextLoads() throws Exception {

		Assertions.assertEquals(environment.getProperty("user.name"), "hello");
		Assertions.assertEquals(environment.getProperty("user.age"), "12");
		Assertions.assertEquals(environment.getProperty("user.gender"), "male");
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ NacosConfigEndpointAutoConfiguration.class,
			NacosConfigAutoConfiguration.class, NacosConfigBootstrapConfiguration.class })
	public static class TestConfig {

	}

}
