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
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.alibaba.dubbo.metadata.ServiceRestMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.resolver.MetadataResolver;
import org.springframework.cloud.alibaba.dubbo.metadata.service.MetadataConfigService;
import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The Auto-Configuration class for Dubbo REST metadata registration,
 * REST metadata that is a part of {@link Registration#getMetadata() Spring Cloud service instances' metadata}
 * will be registered Spring Cloud registry.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@ConditionalOnMissingBean(value = {
        MetadataResolver.class,
        MetadataConfigService.class
})
@AutoConfigureAfter(value = {DubboMetadataAutoConfiguration.class})
@Configuration
public class DubboRestMetadataRegistrationAutoConfiguration {

    /**
     * A Map to store REST metadata temporary, its' key is the special service name for a Dubbo service,
     * the value is a JSON content of JAX-RS or Spring MVC REST metadata from the annotated methods.
     */
    private final Set<ServiceRestMetadata> serviceRestMetadata = new LinkedHashSet<>();

    @Autowired
    private MetadataResolver metadataResolver;

    @Autowired
    private MetadataConfigService metadataConfigService;

    @EventListener(ServiceBeanExportedEvent.class)
    public void recordRestMetadata(ServiceBeanExportedEvent event) throws JsonProcessingException {
        ServiceBean serviceBean = event.getServiceBean();
        serviceRestMetadata.addAll(metadataResolver.resolveServiceRestMetadata(serviceBean));
    }

    /**
     * Pre-handle Spring Cloud application service registered:
     * <p>
     * Put <code>restMetadata</code> with the JSON format into
     * {@link Registration#getMetadata() service instances' metadata}
     * <p>
     *
     * @param event {@link InstancePreRegisteredEvent} instance
     */
    @EventListener(InstancePreRegisteredEvent.class)
    public void registerRestMetadata(InstancePreRegisteredEvent event) throws Exception {
        Registration registration = event.getRegistration();
        metadataConfigService.publishServiceRestMetadata(registration.getServiceId(), serviceRestMetadata);
    }

}
