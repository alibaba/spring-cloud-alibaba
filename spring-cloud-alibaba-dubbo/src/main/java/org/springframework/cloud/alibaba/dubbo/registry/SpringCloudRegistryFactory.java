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
import org.apache.dubbo.common.utils.NamedThreadFactory;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.RegistryFactory;

import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.ScheduledExecutorService;

import static java.lang.System.getProperty;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Dubbo {@link RegistryFactory} uses Spring Cloud Service Registration abstraction, whose protocol is "spring-cloud"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RegistryFactory
 * @see SpringCloudRegistry
 */
public class SpringCloudRegistryFactory implements RegistryFactory {

    private static String SERVICES_LOOKUP_SCHEDULER_THREAD_NAME_PREFIX =
            getProperty("dubbo.services.lookup.scheduler.thread.name.prefix ", "dubbo-services-lookup-");

    private static ConfigurableApplicationContext applicationContext;

    private final ScheduledExecutorService servicesLookupScheduler;

    private DiscoveryClient discoveryClient;

    private DubboServiceMetadataRepository dubboServiceMetadataRepository;

    private volatile boolean initialized = false;

    public SpringCloudRegistryFactory() {
        servicesLookupScheduler = newSingleThreadScheduledExecutor(
                new NamedThreadFactory(SERVICES_LOOKUP_SCHEDULER_THREAD_NAME_PREFIX));
    }

    protected void init() {
        if (initialized || applicationContext == null) {
            return;
        }
        this.discoveryClient = applicationContext.getBean(DiscoveryClient.class);
        this.dubboServiceMetadataRepository = applicationContext.getBean(DubboServiceMetadataRepository.class);
    }

    @Override
    public Registry getRegistry(URL url) {
        init();
        return new SpringCloudRegistry(url, discoveryClient, servicesLookupScheduler, dubboServiceMetadataRepository);
    }

    public static void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        SpringCloudRegistryFactory.applicationContext = applicationContext;
    }
}
