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

import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;

import feign.Contract;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.cloud.alibaba.dubbo.metadata.DubboProtocolConfigSupplier;
import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.alibaba.dubbo.metadata.resolver.DubboServiceBeanMetadataResolver;
import org.springframework.cloud.alibaba.dubbo.metadata.resolver.MetadataResolver;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceFactory;
import org.springframework.cloud.alibaba.dubbo.service.DubboMetadataServiceExporter;
import org.springframework.cloud.alibaba.dubbo.service.DubboMetadataServiceProxy;
import org.springframework.cloud.alibaba.dubbo.service.IntrospectiveDubboMetadataService;
import org.springframework.cloud.alibaba.dubbo.util.JSONUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Spring Boot Auto-Configuration class for Dubbo Metadata
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration
@Import({DubboServiceMetadataRepository.class,
        IntrospectiveDubboMetadataService.class,
        DubboMetadataServiceExporter.class,
        JSONUtils.class})
public class DubboMetadataAutoConfiguration {

    @Autowired
    private ObjectProvider<DubboServiceMetadataRepository> dubboServiceMetadataRepository;

    @Autowired
    private MetadataResolver metadataResolver;

    @Autowired
    private DubboMetadataServiceExporter dubboMetadataConfigServiceExporter;

    @Bean
    @ConditionalOnMissingBean
    public MetadataResolver metadataJsonResolver(ObjectProvider<Contract> contract) {
        return new DubboServiceBeanMetadataResolver(contract);
    }

    @Bean
    public Supplier<ProtocolConfig> dubboProtocolConfigSupplier(ObjectProvider<Collection<ProtocolConfig>> protocols) {
        return new DubboProtocolConfigSupplier(protocols);
    }

    @Bean
    @ConditionalOnMissingBean
    public DubboMetadataServiceProxy dubboMetadataConfigServiceProxy(DubboGenericServiceFactory factory) {
        return new DubboMetadataServiceProxy(factory);
    }

    // Event-Handling

    @EventListener(ServiceBeanExportedEvent.class)
    public void onServiceBeanExported(ServiceBeanExportedEvent event) {
        ServiceBean serviceBean = event.getServiceBean();
        publishServiceRestMetadata(serviceBean);
    }

    @EventListener(ApplicationFailedEvent.class)
    public void onApplicationFailed() {
        unExportDubboMetadataConfigService();
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        unExportDubboMetadataConfigService();
    }

    private void publishServiceRestMetadata(ServiceBean serviceBean) {
        dubboServiceMetadataRepository.getIfAvailable().publishServiceRestMetadata(metadataResolver.resolveServiceRestMetadata(serviceBean));
    }

    private void unExportDubboMetadataConfigService() {
        dubboMetadataConfigServiceExporter.unexport();
    }
}