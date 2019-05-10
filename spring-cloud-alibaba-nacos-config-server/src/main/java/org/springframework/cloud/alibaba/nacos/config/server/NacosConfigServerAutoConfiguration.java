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

package org.springframework.cloud.alibaba.nacos.config.server;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alibaba.nacos.config.server.environment.NacosEnvironmentProperties;
import org.springframework.cloud.alibaba.nacos.config.server.environment.NacosEnvironmentRepository;
import org.springframework.cloud.alibaba.nacos.config.server.environment.NacosEnvironmentRepositoryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Nacos config server Auto-Configuration.
 * <p>
 * create date: 2019-05-07
 *
 * @author cao.yong
 */
@Configuration
@EnableConfigurationProperties(NacosEnvironmentProperties.class)
@Import({NacosRepositoryConfiguration.class})
public class NacosConfigServerAutoConfiguration {
    @Bean
    public NacosEnvironmentRepositoryFactory nacosEnvironmentRepositoryFactory(ConfigurableEnvironment environment) {
        return new NacosEnvironmentRepositoryFactory(environment);
    }
}

/**
 * Want configuration to take effect,
 * must be set ${spring.profiles.active: nacos}
 */
@Configuration
@Profile("nacos")
class NacosRepositoryConfiguration {
    @Bean
    public NacosEnvironmentRepository nacosEnvironmentRepository(NacosEnvironmentRepositoryFactory factory, NacosEnvironmentProperties properties) {
        return factory.build(properties);
    }
}
