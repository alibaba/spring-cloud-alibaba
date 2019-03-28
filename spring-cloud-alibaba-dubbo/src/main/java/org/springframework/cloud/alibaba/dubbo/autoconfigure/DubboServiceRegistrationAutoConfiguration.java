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

import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;

import com.ecwid.consul.v1.agent.model.NewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.alibaba.dubbo.registry.DubboServiceRegistrationEventPublishingAspect;
import org.springframework.cloud.alibaba.dubbo.registry.event.ServiceInstancePreRegisteredEvent;
import org.springframework.cloud.alibaba.dubbo.service.DubboMetadataConfigServiceExporter;
import org.springframework.cloud.alibaba.dubbo.util.JSONUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.DUBBO_URLS_METADATA_PROPERTY_NAME;

/**
 * Dubbo Service Registration Auto-{@link Configuration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration
@Import({DubboServiceRegistrationEventPublishingAspect.class})
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
public class DubboServiceRegistrationAutoConfiguration {

    private static final String CONSUL_REGISTRATION_CLASS_NAME =
            "org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration";

    private static final String EUREKA_REGISTRATION_CLASS_NAME =
            "org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    private DubboServiceMetadataRepository dubboServiceMetadataRepository;

    @Autowired
    private DubboMetadataConfigServiceExporter dubboMetadataConfigServiceExporter;

    @Autowired
    private JSONUtils jsonUtils;

    private Registration registration;

    @ConditionalOnClass(name = EUREKA_REGISTRATION_CLASS_NAME)
    @Bean
    public ApplicationListener<ServiceBeanExportedEvent> onServiceBeanExportedInEureka() {
        return event -> {
            reRegister();
        };
    }

    private void reRegister() {
        Registration registration = this.registration;
        if (registration == null) {
            return;
        }
        serviceRegistry.register(registration);
    }


    // Event-Handling

    @EventListener(ServiceInstancePreRegisteredEvent.class)
    public void onServiceInstancePreRegistered(ServiceInstancePreRegisteredEvent event) {

        dubboMetadataConfigServiceExporter.export();

        Registration registration = event.getSource();
        attachURLsIntoMetadata(registration);
        setRegistration(registration);
    }

    private void setRegistration(Registration registration) {
        this.registration = registration;
    }

    /**
     * Handle the pre-registered event of {@link ServiceInstance} for Consul
     *
     * @return ApplicationListener<ServiceInstancePreRegisteredEvent>
     */
    @ConditionalOnClass(name = CONSUL_REGISTRATION_CLASS_NAME)
    @Bean
    public ApplicationListener<ServiceInstancePreRegisteredEvent> onServiceInstancePreRegisteredInConsul() {
        return event -> {
            Registration registration = event.getSource();
            String registrationClassName = registration.getClass().getName();
            if (CONSUL_REGISTRATION_CLASS_NAME.equalsIgnoreCase(registrationClassName)) {
                NewService newService = ((ConsulRegistration) registration).getService();
                String dubboURLsJson = getDubboURLsJSON();
                if (StringUtils.hasText(dubboURLsJson)) {
                    List<String> tags = newService.getTags();
                    tags.add(DUBBO_URLS_METADATA_PROPERTY_NAME + "=" + dubboURLsJson);
                }
            }
        };
    }

    private void attachURLsIntoMetadata(Registration registration) {
        if (registration == null) {
            return;
        }
        Map<String, String> metadata = registration.getMetadata();
        String dubboURLsJson = getDubboURLsJSON();
        if (StringUtils.hasText(dubboURLsJson)) {
            metadata.put(DUBBO_URLS_METADATA_PROPERTY_NAME, dubboURLsJson);
        }
    }

    private String getDubboURLsJSON() {
        Collection<URL> urls = dubboServiceMetadataRepository.getRegisteredUrls();
        if (CollectionUtils.isEmpty(urls)) {
            if (logger.isDebugEnabled()) {
                logger.debug("There is no registered URL to attach into metadata.");
            }
            return null;
        }
        return jsonUtils.toJSON(urls.stream().map(URL::toFullString).collect(Collectors.toList()));
    }
}
