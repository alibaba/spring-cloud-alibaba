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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySourceLocator;
import org.springframework.cloud.alibaba.nacos.refresh.NacosRefreshProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiaojing
 */
public class NacosConfigAutoConfigurationTests {

	private ConfigurableApplicationContext context;

	@Before
	public void setUp() throws Exception {
		this.context = new SpringApplicationBuilder(
				NacosConfigBootstrapConfiguration.class,
				NacosConfigAutoConfiguration.class, TestConfiguration.class)
						.web(WebApplicationType.NONE)
						.run("--spring.application.name=myapp",
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
	public void testNacosConfigProperties() {

		NacosConfigProperties nacosConfigProperties = this.context.getParent()
				.getBean(NacosConfigProperties.class);
		assertThat(nacosConfigProperties.getFileExtension()).isEqualTo("properties");
		assertThat(nacosConfigProperties.getPrefix()).isEqualTo("test");
		assertThat(nacosConfigProperties.getName()).isEqualTo("myapp");

	}

	@Test
	public void testNacosRefreshProperties() {

		NacosRefreshProperties nacosRefreshProperties = this.context
				.getBean(NacosRefreshProperties.class);
		assertThat(nacosRefreshProperties.isEnabled()).isEqualTo(true);

	}

	@Configuration
	@AutoConfigureBefore(NacosConfigAutoConfiguration.class)
	static class TestConfiguration {

		@Autowired
		ConfigurableApplicationContext context;

		@Bean
		ContextRefresher contextRefresher() {
			return new ContextRefresher(context, new RefreshScope());
		}

	}

}
