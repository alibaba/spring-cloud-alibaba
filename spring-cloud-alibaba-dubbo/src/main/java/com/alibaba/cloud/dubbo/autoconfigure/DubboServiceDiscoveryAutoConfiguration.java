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
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceDiscoveryAutoConfiguration.CONSUL_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME;
import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceDiscoveryAutoConfiguration.ZOOKEEPER_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME;
import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceRegistrationAutoConfiguration.EUREKA_CLIENT_AUTO_CONFIGURATION_CLASS_NAME;
import static org.apache.zookeeper.Watcher.Event.EventType.NodeChildrenChanged;
import static org.apache.zookeeper.Watcher.Event.EventType.NodeDataChanged;
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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ApplicationEventPublisher applicationEventPublisher;

    private final DiscoveryClient discoveryClient;

    public DubboServiceDiscoveryAutoConfiguration(DubboServiceMetadataRepository dubboServiceMetadataRepository,
                                                  ApplicationEventPublisher applicationEventPublisher,
                                                  DiscoveryClient discoveryClient) {
        this.dubboServiceMetadataRepository = dubboServiceMetadataRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.discoveryClient = discoveryClient;
    }


    private void forEachSubscribedServices(Consumer<String> serviceNameConsumer) {
        dubboServiceMetadataRepository.getSubscribedServices().forEach(serviceNameConsumer);
    }

    /**
     * Dispatch a {@link ServiceInstancesChangedEvent}
     *
     * @param serviceName      the name of service
     * @param serviceInstances the {@link ServiceInstance instances} of some service
     * @see AbstractSpringCloudRegistry#registerServiceInstancesChangedEventListener(URL, NotifyListener)
     */
    private void dispatchServiceInstancesChangedEvent(String serviceName, Collection<ServiceInstance> serviceInstances) {
        if (!hasText(serviceName) || serviceInstances == null) {
            return;
        }
        ServiceInstancesChangedEvent event = new ServiceInstancesChangedEvent(serviceName, serviceInstances);
        applicationEventPublisher.publishEvent(event);
    }

    private List<ServiceInstance> getInstances(String serviceName) {
        return discoveryClient.getInstances(serviceName);
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
            forEachSubscribedServices(serviceName -> {
                List<ServiceInstance> serviceInstances = getInstances(serviceName);
                dispatchServiceInstancesChangedEvent(serviceName, serviceInstances);
            });
        }

    }

    @Configuration
    @ConditionalOnBean(name = ZOOKEEPER_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME)
    class ZookeeperConfiguration {

        /**
         * The Key is watched Zookeeper path, the value is an instance of {@link CuratorWatcher}
         */
        private final Map<String, CuratorWatcher> watcherCaches = new ConcurrentHashMap<>();

        private final ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;

        private final ObjectProvider<ZookeeperDependencies> zookeeperDependencies;

        private final CuratorFramework curatorFramework;

        private final String rootPath;

        ZookeeperConfiguration(ZookeeperDiscoveryProperties zookeeperDiscoveryProperties,
                               ObjectProvider<ZookeeperDependencies> zookeeperDependencies,
                               CuratorFramework curatorFramework) {
            this.zookeeperDiscoveryProperties = zookeeperDiscoveryProperties;
            this.zookeeperDependencies = zookeeperDependencies;
            this.curatorFramework = curatorFramework;
            this.rootPath = zookeeperDiscoveryProperties.getRoot();
        }

        @EventListener(ContextRefreshedEvent.class)
        public void onContextRefreshedEvent(ContextRefreshedEvent event) {
            forEachSubscribedServices(this::registerServiceWatcher);
        }

        private void registerServiceWatcher(String serviceName) {

            String servicePath = buildServicePath(serviceName);

            CuratorWatcher watcher = watcherCaches.computeIfAbsent(servicePath,
                    path -> new ServiceInstancesChangedWatcher(serviceName));

            try {
                curatorFramework.getChildren().usingWatcher(watcher).forPath(servicePath);
            } catch (KeeperException.NoNodeException e) {
                // ignored
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage());
                }
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        class ServiceInstancesChangedWatcher implements CuratorWatcher {

            private final String serviceName;

            ServiceInstancesChangedWatcher(String serviceName) {
                this.serviceName = serviceName;
            }

            @Override
            public void process(WatchedEvent event) throws Exception {

                Watcher.Event.EventType eventType = event.getType();

                if (NodeChildrenChanged.equals(eventType) || NodeDataChanged.equals(eventType)) {
                    dispatchServiceInstancesChangedEvent(serviceName, getInstances(serviceName));
                }

                // re-register again
                registerServiceWatcher(serviceName);
            }
        }

        private String buildServicePath(String serviceName) {
            return rootPath + "/" + serviceRelativePath(serviceName);
        }

        private String serviceRelativePath(String serviceName) {
            return Optional.ofNullable(zookeeperDependencies.getIfAvailable())
                    .map(z -> z.getAliasForPath(serviceName))
                    .orElse(serviceName);
        }

    }

    @Configuration
    @ConditionalOnBean(name = CONSUL_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME)
    @AutoConfigureOrder
    class ConsulConfiguration {

    }
}
