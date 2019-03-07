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

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.alibaba.dubbo.metadata.resolver.MetadataResolver;
import org.springframework.cloud.alibaba.dubbo.service.DubboMetadataConfigService;
import org.springframework.cloud.alibaba.dubbo.service.PublishingDubboMetadataConfigService;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.alibaba.dubbo.autoconfigure.DubboMetadataAutoConfiguration.METADATA_PROTOCOL_BEAN_NAME;

/**
 * The Auto-Configuration class for Dubbo REST metadata registration,
 * REST metadata that is a part of {@link Registration#getMetadata() Spring Cloud service instances' metadata}
 * will be registered Spring Cloud registry.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@ConditionalOnBean(value = {
        MetadataResolver.class
})
@AutoConfigureAfter(value = {DubboMetadataAutoConfiguration.class})
@Configuration
public class DubboRestMetadataRegistrationAutoConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MetadataResolver metadataResolver;

    @Autowired
    private PublishingDubboMetadataConfigService dubboMetadataConfigService;

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    @Qualifier(METADATA_PROTOCOL_BEAN_NAME)
    private ProtocolConfig metadataProtocolConfig;

    @Value("${spring.application.name:application}")
    private String currentApplicationName;

    /**
     * The ServiceConfig of DubboMetadataConfigService to be exported, can be nullable.
     */
    private ServiceConfig<DubboMetadataConfigService> serviceConfig;

    @EventListener(ServiceBeanExportedEvent.class)
    public void recordRestMetadata(ServiceBeanExportedEvent event) {
        ServiceBean serviceBean = event.getServiceBean();
        dubboMetadataConfigService.publishServiceRestMetadata(metadataResolver.resolveServiceRestMetadata(serviceBean));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        exportDubboMetadataConfigService();
    }

    @EventListener(ApplicationFailedEvent.class)
    public void onApplicationFailed() {
        unexportDubboMetadataConfigService();
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        unexportDubboMetadataConfigService();
    }

    private void exportDubboMetadataConfigService() {

        if (serviceConfig != null && serviceConfig.isExported()) {
            return;
        }

        if (StringUtils.isEmpty(dubboMetadataConfigService.getServiceRestMetadata())) {
            // If there is no REST metadata, DubboMetadataConfigService will not be exported.
            if (logger.isInfoEnabled()) {
                logger.info("There is no REST metadata, the Dubbo service[{}] will not be exported.",
                        dubboMetadataConfigService.getClass().getName());
            }
            return;
        }

        serviceConfig = new ServiceConfig<>();

        serviceConfig.setInterface(DubboMetadataConfigService.class);
        // Use current Spring application name as the Dubbo Service version
        serviceConfig.setVersion(currentApplicationName);
        serviceConfig.setRef(dubboMetadataConfigService);
        serviceConfig.setApplication(applicationConfig);
        serviceConfig.setProtocol(metadataProtocolConfig);

        serviceConfig.export();

        if (logger.isInfoEnabled()) {
            logger.info("The Dubbo service[{}] has been exported.", serviceConfig.toString());
        }
    }

    private void unexportDubboMetadataConfigService() {

        if (serviceConfig == null || serviceConfig.isUnexported()) {
            return;
        }

        serviceConfig.unexport();

        if (logger.isInfoEnabled()) {
            logger.info("The Dubbo service[{}] has been unexported.", serviceConfig.toString());
        }
    }
}
