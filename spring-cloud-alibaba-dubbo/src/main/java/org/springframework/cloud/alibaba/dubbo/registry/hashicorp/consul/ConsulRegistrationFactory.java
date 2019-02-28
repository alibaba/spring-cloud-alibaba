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
package org.springframework.cloud.alibaba.dubbo.registry.hashicorp.consul;

import com.alibaba.dubbo.common.URL;

import com.ecwid.consul.v1.agent.model.NewService;
import org.springframework.cloud.alibaba.dubbo.registry.AbstractRegistrationFactory;
import org.springframework.cloud.alibaba.dubbo.registry.RegistrationFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.ConsulServerUtils;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.context.ApplicationContext;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link ConsulRegistration} {@link RegistrationFactory} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class ConsulRegistrationFactory extends AbstractRegistrationFactory<ConsulRegistration> {

    @Override
    public ConsulRegistration create(String serviceName, URL url, ApplicationContext applicationContext) {
        ServiceInstance serviceInstance = createServiceInstance(serviceName, url);

        Map<String, String> metadata = getMetadata(serviceInstance);
        List<String> tags = createTags(metadata);

        NewService service = new NewService();
        service.setId(serviceInstance.getInstanceId());
        service.setName(serviceInstance.getServiceId());
        service.setAddress(serviceInstance.getHost());
        service.setPort(serviceInstance.getPort());
        service.setMeta(metadata);
        service.setTags(tags);

        ConsulDiscoveryProperties properties = applicationContext.getBean(ConsulDiscoveryProperties.class);

        ConsulRegistration registration = new ConsulRegistration(service, properties);
        return registration;
    }

    /**
     * @param metadata
     * @return
     * @see ConsulServerUtils#getMetadata(java.util.List)
     */
    private List<String> createTags(Map<String, String> metadata) {
        List<String> tags = new LinkedList<>();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String tag = entry.getKey() + "=" + entry.getValue();
            tags.add(tag);

        }
        return tags;
    }

    private Map<String, String> getMetadata(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        Set<String> removedKeys = new LinkedHashSet<>();
        for (String key : metadata.keySet()) {
            if (key.contains(".")) {
                removedKeys.add(key);
            }
        }
        for (String removedKey : removedKeys) {
            metadata.remove(removedKey);
        }
        return metadata;
    }
}
