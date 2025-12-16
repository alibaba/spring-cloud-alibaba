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

package com.alibaba.cloud.nacos.configdata;


import com.alibaba.cloud.nacos.NacosConfigBootstrapConfiguration;
import com.alibaba.cloud.nacos.NacosConfigManager;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NacosConfigManagerTests {

	ConfigurableApplicationContext context;

	@Test
	public void testNacosConfigManagerIsNotExists() {
		context = new SpringApplicationBuilder(Configuration.class)
				.web(WebApplicationType.NONE)
				.properties("spring.cloud.nacos.server-addr=123.123.123.123:8848")
				.properties("spring.cloud.nacos.config.import-check.enabled=false").run();
		assertThatThrownBy(() -> context.getBean(NacosConfigManager.class)).isInstanceOf(NoSuchBeanDefinitionException.class)
				.hasMessage("No qualifying bean of type 'com.alibaba.cloud.nacos.NacosConfigManager' available");
	}


	@Test
	public void testNacosConfigManagerIsExists() {
		context = new SpringApplicationBuilder(Configuration.class)
				.web(WebApplicationType.NONE)
				.properties("spring.cloud.nacos.server-addr=123.123.123.123:8848")
				.properties("spring.cloud.nacos.config.import-check.enabled=false")
				.properties("spring.config.import=nacos:test.properties").run();
		context.getBean(NacosConfigManager.class);
		assertThat(context.getBean(NacosConfigManager.class)).isInstanceOf(NacosConfigManager.class);
	}

	@org.springframework.context.annotation.Configuration
	@ImportAutoConfiguration({NacosConfigBootstrapConfiguration.class})
	@EnableAutoConfiguration
	public static class Configuration {

	}
}
