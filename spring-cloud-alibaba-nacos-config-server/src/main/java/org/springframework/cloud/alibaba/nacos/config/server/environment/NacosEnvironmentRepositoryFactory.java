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

package org.springframework.cloud.alibaba.nacos.config.server.environment;

import org.springframework.cloud.config.server.environment.EnvironmentRepositoryFactory;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Nacos environment repository factory.
 *
 * create date: 2019-05-07
 *
 * @author cao.yong
 */
public class NacosEnvironmentRepositoryFactory implements EnvironmentRepositoryFactory<NacosEnvironmentRepository, NacosEnvironmentProperties> {

    private ConfigurableEnvironment environment;

    public NacosEnvironmentRepositoryFactory(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public NacosEnvironmentRepository build(NacosEnvironmentProperties environmentProperties) {
        return new NacosEnvironmentRepository(environment, environmentProperties);
    }
}
