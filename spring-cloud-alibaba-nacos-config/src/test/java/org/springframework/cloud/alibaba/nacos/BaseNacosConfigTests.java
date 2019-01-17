/*
 * Copyright (C) 2019 the original author or authors.
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySourceBuilder;
import org.springframework.cloud.alibaba.nacos.endpoint.NacosConfigEndpointAutoConfiguration;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.nacos.api.config.ConfigService;

/**
 * @author pbting
 * @date 2019-01-17 11:45 AM
 */
public abstract class BaseNacosConfigTests {

	protected ConfigurableApplicationContext context;

	@Before
	public void setUp() throws Exception {
		this.context = new SpringApplicationBuilder(
				NacosConfigBootstrapConfiguration.class,
				NacosConfigEndpointAutoConfiguration.class,
				NacosConfigAutoConfiguration.class, TestConfiguration.class)
						.web(WebApplicationType.SERVLET)
						.run("--spring.cloud.nacos.config.name=sca-nacos-config",
								"--spring.cloud.config.enabled=true",
								"--server.port=18080",
								"--spring.application.name=sca-nacos-config",
								"--spring.cloud.nacos.config.server-addr=127.0.0.1:8848",
								// "--spring.cloud.nacos.config.prefix=test",
								"--spring.cloud.nacos.config.encode=utf-8",
								// "--spring.cloud.nacos.config.file-extension=yaml",
								"--spring.profiles.active=develop",
								"--spring.cloud.nacos.config.shared-data-ids=base-common.properties,common.properties",
								"--spring.cloud.nacos.config.refreshable-dataids=common.properties",
								"--spring.cloud.nacos.config.ext-config[0].data-id=ext00.yaml",
								"--spring.cloud.nacos.config.ext-config[1].data-id=ext01.yaml",
								"--spring.cloud.nacos.config.ext-config[1].group=EXT01_GROUP",
								"--spring.cloud.nacos.config.ext-config[1].refresh=true",
								"--spring.cloud.nacos.config.ext-config[2].data-id=ext02.yaml");
	}

	public NacosPropertySourceBuilder nacosPropertySourceBuilderInstance() {
		NacosConfigProperties nacosConfigProperties = this.context
				.getBean(NacosConfigProperties.class);

		ConfigService configService = nacosConfigProperties.configServiceInstance();
		long timeout = nacosConfigProperties.getTimeout();
		NacosPropertySourceBuilder nacosPropertySourceBuilder = new NacosPropertySourceBuilder(
				configService, timeout);
		return nacosPropertySourceBuilder;
	}

	@After
	public void tearDown() throws Exception {
		if (this.context != null) {
			this.context.close();
		}
	}

	@EnableAutoConfiguration
	@Configuration
	@AutoConfigureBefore(NacosConfigAutoConfiguration.class)
	static class TestConfiguration {

		@Autowired
		ConfigurableApplicationContext context;

		@Bean
		ContextRefresher contextRefresher() {
			RefreshScope refreshScope = new RefreshScope();
			refreshScope.setApplicationContext(context);
			return new ContextRefresher(context, refreshScope);
		}
	}
}