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
package org.springframework.cloud.alibaba.dubbo.registry;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.RegistryFactory;

import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Dubbo {@link RegistryFactory} uses Spring Cloud Service Registration abstraction, whose protocol is "spring-cloud"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class SpringCloudRegistry extends AbstractSpringCloudRegistry {

    private final DubboServiceMetadataRepository dubboServiceMetadataRepository;

    public SpringCloudRegistry(URL url, DiscoveryClient discoveryClient,
                               ScheduledExecutorService servicesLookupScheduler,
                               DubboServiceMetadataRepository dubboServiceMetadataRepository) {
        super(url, discoveryClient, servicesLookupScheduler);
        this.dubboServiceMetadataRepository = dubboServiceMetadataRepository;
    }

    @Override
    protected void doRegister0(URL url) {
        dubboServiceMetadataRepository.registerURL(url);
    }

    @Override
    protected void doUnregister0(URL url) {
        dubboServiceMetadataRepository.unregisterURL(url);
    }

    @Override
    protected boolean supports(String serviceName) {
        return dubboServiceMetadataRepository.isSubscribedService(serviceName);
    }

    @Override
    protected String getServiceName(URL url) {
        return dubboServiceMetadataRepository.getServiceName(url);
    }

    @Override
    protected void notifySubscriber(URL url, NotifyListener listener, List<ServiceInstance> serviceInstances) {
        List<URL> urls = serviceInstances.stream()
                .map(dubboServiceMetadataRepository::buildURLs)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        notify(url, listener, urls);
    }
}
