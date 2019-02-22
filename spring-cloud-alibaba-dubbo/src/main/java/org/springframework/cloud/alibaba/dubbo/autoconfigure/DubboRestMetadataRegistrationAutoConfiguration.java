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

import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.config.spring.context.event.ServiceBeanExportedEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.alibaba.dubbo.metadata.resolver.MetadataResolver;
import org.springframework.cloud.alibaba.dubbo.service.PublishingDubboMetadataConfigService;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

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

    @Autowired
    private MetadataResolver metadataResolver;

    @Autowired
    private PublishingDubboMetadataConfigService dubboMetadataConfigService;

    @Value("${spring.application.name:application}")
    private String currentApplicationName;

    @EventListener(ServiceBeanExportedEvent.class)
    public void recordRestMetadata(ServiceBeanExportedEvent event) {
        ServiceBean serviceBean = event.getServiceBean();
        dubboMetadataConfigService.publishServiceRestMetadata(metadataResolver.resolveServiceRestMetadata(serviceBean));
    }
}
