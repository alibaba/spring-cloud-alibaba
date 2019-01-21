/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.alibaba.dubbo.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.alibaba.dubbo.metadata.service.MetadataConfigService;
import org.springframework.cloud.alibaba.dubbo.metadata.service.NacosMetadataConfigService;
import org.springframework.cloud.alibaba.nacos.NacosConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot Auto-Configuration class for Dubbo Metadata
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration
@Import(DubboServiceMetadataRepository.class)
public class DubboMetadataAutoConfiguration {

    @Bean
    @ConditionalOnBean(NacosConfigProperties.class)
    public MetadataConfigService metadataConfigService() {
        return new NacosMetadataConfigService();
    }

}