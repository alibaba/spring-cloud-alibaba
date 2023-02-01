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

import com.alibaba.cloud.nacos.client.NacosPropertySourceLocator;
import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpointAutoConfiguration;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.NacosConfigService;
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

import static com.alibaba.cloud.nacos.NacosConfigurationExtConfigTests.TestConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 *
 * @author xiaojing
 * @author freeman
 */
@SpringBootTest(classes = TestConfig.class, webEnvironment = NONE, properties = {
		"spring.application.name=myTestService1", "spring.profiles.active=dev,test",
		"spring.cloud.nacos.config.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.config.encode=utf-8",
		"spring.cloud.nacos.config.timeout=1000",
		"spring.cloud.nacos.config.file-extension=properties",
		"spring.cloud.nacos.config.ext-config[0].data-id=ext-config-common01.properties",
		"spring.cloud.nacos.config.ext-config[1].data-id=ext-config-common02.properties",
		"spring.cloud.nacos.config.ext-config[1].group=GLOBAL_GROUP",
		"spring.cloud.nacos.config.shared-dataids=common1.properties,common2.properties",
		"spring.cloud.nacos.config.accessKey=test-accessKey",
		"spring.cloud.nacos.config.secretKey=test-secretKey",
		"spring.cloud.bootstrap.enabled=true" })
public class NacosConfigurationExtConfigTests {

	@Autowired
	private Environment environment;

	@Autowired
	private NacosPropertySourceLocator locator;

	@Autowired
	private NacosConfigProperties properties;

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
							if ("test-name.properties".equals(dataId)
									&& "DEFAULT_GROUP".equals(group)) {
								return "user.name=hello\nuser.age=12";
							}

							if ("test-name-dev.properties".equals(dataId)
									&& "DEFAULT_GROUP".equals(group)) {
								return "user.name=dev";
							}

							if ("ext-config-common01.properties".equals(dataId)
									&& "DEFAULT_GROUP".equals(group)) {
								return "test-ext-config1=config1\ntest-ext-config2=config1";
							}
							if ("ext-config-common02.properties".equals(dataId)
									&& "GLOBAL_GROUP".equals(group)) {
								return "test-ext-config2=config2";
							}

							if ("common1.properties".equals(dataId)
									&& "DEFAULT_GROUP".equals(group)) {
								return "test-common1=common1\ntest-common2=common1";
							}

							if ("common2.properties".equals(dataId)
									&& "DEFAULT_GROUP".equals(group)) {
								return "test-common2=common2";
							}

							return "";
						}

					});

			ReflectionTestUtils.setField(NacosConfigManager.class, "service",
					mockedNacosConfigService);
		}
		catch (NacosException ignored) {
			ignored.printStackTrace();
		}
	}

	@Test
	public void contextLoads() throws Exception {

		assertThat(locator).isNotNull();
		assertThat(properties).isNotNull();

		assertThat(environment.getProperty("test-ext-config1")).isEqualTo("config1");
		assertThat(environment.getProperty("test-ext-config2")).isEqualTo("config2");
		assertThat(environment.getProperty("test-common1")).isEqualTo("common1");
		assertThat(environment.getProperty("test-common2")).isEqualTo("common2");
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ NacosConfigEndpointAutoConfiguration.class,
			NacosConfigAutoConfiguration.class, NacosConfigBootstrapConfiguration.class })
	public static class TestConfig {

	}

}
