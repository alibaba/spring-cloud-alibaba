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
package com.alibaba.cloud.dubbo.autoconfigure;

import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.registry.event.ServiceInstancesChangedEvent;
import com.netflix.discovery.CacheRefreshedEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.Collection;
import java.util.List;

import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceDiscoveryAutoConfiguration.CONSUL_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME;
import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceDiscoveryAutoConfiguration.ZOOKEEPER_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME;
import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceRegistrationAutoConfiguration.EUREKA_CLIENT_AUTO_CONFIGURATION_CLASS_NAME;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

/**
 * Dubbo Service Discovery Auto {@link Configuration} (after {@link DubboServiceRegistrationAutoConfiguration})
 *
 * @see DubboServiceRegistrationAutoConfiguration
 * @see Configuration
 * @see DiscoveryClient
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.cloud.client.discovery.DiscoveryClient")
@ConditionalOnProperty(name = "spring.cloud.discovery.enabled", matchIfMissing = true)
@AutoConfigureAfter(name = {
        EUREKA_CLIENT_AUTO_CONFIGURATION_CLASS_NAME,
        ZOOKEEPER_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME,
        CONSUL_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME
}, value = {DubboServiceRegistrationAutoConfiguration.class})
public class DubboServiceDiscoveryAutoConfiguration {

    public static final String ZOOKEEPER_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME = "org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration";

    public static final String CONSUL_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME = "org.springframework.cloud.consul.discovery.ConsulDiscoveryClientConfiguration";

    private final DubboServiceMetadataRepository dubboServiceMetadataRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final ObjectProvider<DiscoveryClient> discoveryClient;

    public DubboServiceDiscoveryAutoConfiguration(DubboServiceMetadataRepository dubboServiceMetadataRepository,
                                                  ApplicationEventPublisher applicationEventPublisher,
                                                  ObjectProvider<DiscoveryClient> discoveryClient) {
        this.dubboServiceMetadataRepository = dubboServiceMetadataRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.discoveryClient = discoveryClient;
    }


    /**
     * Dispatch a {@link ServiceInstancesChangedEvent}
     *
     * @param serviceName      the name of service
     * @param serviceInstances the {@link ServiceInstance instances} of some service
     * @see AbstractSpringCloudRegistry#registerServiceInstancesChangedEventListener(URL, NotifyListener)
     */
    protected void dispatchServiceInstancesChangedEvent(String serviceName, Collection<ServiceInstance> serviceInstances) {
        if (!hasText(serviceName) || isEmpty(serviceInstances)) {
            return;
        }
        ServiceInstancesChangedEvent event = new ServiceInstancesChangedEvent(serviceName, serviceInstances);
        applicationEventPublisher.publishEvent(event);
    }

    @Configuration
    @ConditionalOnBean(name = EUREKA_CLIENT_AUTO_CONFIGURATION_CLASS_NAME)
    class EurekaConfiguration {

        /**
         * Dispatch a {@link ServiceInstancesChangedEvent} when the {@link HeartbeatEvent} raised
         * <p>
         * {@link CloudEurekaClient#onCacheRefreshed()} publishes a HeartbeatEvent instead of {@link CacheRefreshedEvent}
         *
         * @param event the {@link HeartbeatEvent} instance
         * @see HeartbeatEvent
         * @see CloudEurekaClient#onCacheRefreshed()
         * @see CacheRefreshedEvent
         */
        @EventListener(HeartbeatEvent.class)
        public void onHeartbeatEvent(HeartbeatEvent event) {
            discoveryClient.ifAvailable(discoveryClient -> {
                dubboServiceMetadataRepository.getSubscribedServices().forEach(serviceName -> {
                    List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceName);
                    dispatchServiceInstancesChangedEvent(serviceName, serviceInstances);
                });
            });

        }
    }

    @Configuration
    @ConditionalOnBean(name = ZOOKEEPER_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME)
    class ZookeeperConfiguration {

    }

    @Configuration
    @ConditionalOnBean(name = CONSUL_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME)
    @AutoConfigureOrder
    class ConsulConfiguration {

    }
}
