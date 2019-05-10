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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.alibaba.nacos.NacosConfigProperties;
import org.springframework.cloud.config.server.support.EnvironmentRepositoryProperties;
import org.springframework.core.Ordered;

/**
 * Nacos environment properties.
 *
 * create date: 2019-05-07
 *
 * @author cao.yong
 *
 * inherit {@link org.springframework.cloud.alibaba.nacos.NacosConfigProperties}
 */
@ConfigurationProperties(NacosEnvironmentProperties.PREFIX)
public class NacosEnvironmentProperties extends NacosConfigProperties implements EnvironmentRepositoryProperties, Ordered {

    private int order = LOWEST_PRECEDENCE;

    /**
     * 获取 优先级
     *
     * @return order 优先级
     */
    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * 设置 加载优先级
     *
     * @param order 优先级
     */
    @Override
    public void setOrder(int order) {
        this.order = order;
    }
}
