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

package org.springframework.cloud.alibaba.nacos;

import java.lang.reflect.Field;

import com.alibaba.nacos.api.config.ConfigService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySourceLocator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiaojing
 */
public class NacosConfigBootstrapConfigurationTests {

	private ConfigurableApplicationContext context;

	@Before
	public void setUp() throws Exception {
		this.context = new SpringApplicationBuilder(
				NacosConfigBootstrapConfiguration.class).web(WebApplicationType.NONE).run(
						"--spring.application.name=myapp",
						"--spring.cloud.config.enabled=true",
						"--spring.cloud.nacos.config.server-addr=127.0.0.1:8080",
						"--spring.cloud.nacos.config.prefix=test");
	}

	@After
	public void tearDown() throws Exception {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void testNacosPropertySourceLocator() {

		NacosPropertySourceLocator locator = this.context
				.getBean(NacosPropertySourceLocator.class);
		Environment environment = this.context.getEnvironment();
		try {
			locator.locate(environment);
		}
		catch (Exception e) {

		}

		Field nacosConfigPropertiesField = ReflectionUtils
				.findField(NacosPropertySourceLocator.class, "nacosConfigProperties");
		nacosConfigPropertiesField.setAccessible(true);

		NacosConfigProperties configService = (NacosConfigProperties) ReflectionUtils
				.getField(nacosConfigPropertiesField, locator);

		assertThat(configService).isNotNull();
	}

}
