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

import feign.Contract;
import org.apache.dubbo.config.ProtocolConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.alibaba.dubbo.metadata.resolver.DubboServiceBeanMetadataResolver;
import org.springframework.cloud.alibaba.dubbo.metadata.resolver.MetadataResolver;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceFactory;
import org.springframework.cloud.alibaba.dubbo.service.DubboMetadataConfigServiceProxy;
import org.springframework.cloud.alibaba.dubbo.service.PublishingDubboMetadataConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collection;
import java.util.Iterator;

import static com.alibaba.dubbo.common.Constants.DEFAULT_PROTOCOL;

/**
 * Spring Boot Auto-Configuration class for Dubbo Metadata
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration
@Import({DubboServiceMetadataRepository.class, PublishingDubboMetadataConfigService.class})
public class DubboMetadataAutoConfiguration {

    public static final String METADATA_PROTOCOL_BEAN_NAME = "metadata";

    @Bean
    @ConditionalOnMissingBean
    public MetadataResolver metadataJsonResolver(ObjectProvider<Contract> contract) {
        return new DubboServiceBeanMetadataResolver(contract);
    }

    /**
     * Build an alias Bean for {@link ProtocolConfig}
     *
     * @param protocols {@link ProtocolConfig} Beans
     * @return {@link ProtocolConfig} bean
     */
    @Bean(name = METADATA_PROTOCOL_BEAN_NAME)
    public ProtocolConfig protocolConfig(Collection<ProtocolConfig> protocols) {
        ProtocolConfig protocolConfig = null;
        for (ProtocolConfig protocol : protocols) {
            String protocolName = protocol.getName();
            if (DEFAULT_PROTOCOL.equals(protocolName)) {
                protocolConfig = protocol;
                break;
            }
        }

        if (protocolConfig == null) { // If The ProtocolConfig bean named "dubbo" is absent, take first one of them
            Iterator<ProtocolConfig> iterator = protocols.iterator();
            protocolConfig = iterator.hasNext() ? iterator.next() : null;
        }

        if (protocolConfig == null) {
            protocolConfig = new ProtocolConfig();
            protocolConfig.setName(DEFAULT_PROTOCOL);
            protocolConfig.setPort(20880);
        }

        return protocolConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public DubboMetadataConfigServiceProxy dubboMetadataConfigServiceProxy(DubboGenericServiceFactory factory) {
        return new DubboMetadataConfigServiceProxy(factory);
    }
}