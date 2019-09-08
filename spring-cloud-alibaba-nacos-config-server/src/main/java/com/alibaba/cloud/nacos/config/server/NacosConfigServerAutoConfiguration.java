/*
 * Copyright (C) 2018 the original author or authors.
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
package com.alibaba.cloud.nacos.config.server;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.config.server.config.ConfigServerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.nacos.config.server.environment.NacosEnvironmentRepository;

/**
 * Nacos Config Server Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.2.0
 */
@ConditionalOnClass(EnableConfigServer.class) // If class of @EnableConfigServer is
												// present in class-path
@ComponentScan(basePackages = { "com.alibaba.nacos.config.server", })
@AutoConfigureBefore(ConfigServerAutoConfiguration.class)
@Configuration
public class NacosConfigServerAutoConfiguration {

	@Bean
	public NacosEnvironmentRepository nacosEnvironmentRepository() {
		return new NacosEnvironmentRepository();
	}

}
