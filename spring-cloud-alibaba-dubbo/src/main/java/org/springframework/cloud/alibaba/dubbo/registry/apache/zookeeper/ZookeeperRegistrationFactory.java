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
package org.springframework.cloud.alibaba.dubbo.registry.apache.zookeeper;

import com.alibaba.dubbo.common.URL;

import org.springframework.cloud.alibaba.dubbo.registry.AbstractRegistrationFactory;
import org.springframework.cloud.alibaba.dubbo.registry.RegistrationFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.context.ApplicationContext;

/**
 * Zookeeper {@link RegistrationFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class ZookeeperRegistrationFactory extends AbstractRegistrationFactory<ZookeeperRegistration> {

    @Override
    public ZookeeperRegistration create(String serviceName, URL url, ApplicationContext applicationContext) {

        ServiceInstance serviceInstance = createServiceInstance(serviceName, url);

        ZookeeperInstance zookeeperInstance = new ZookeeperInstance(serviceInstance.getInstanceId(),
                serviceInstance.getServiceId(), serviceInstance.getMetadata());

        ZookeeperRegistration registration = ServiceInstanceRegistration
                .builder()
                .address(serviceInstance.getHost())
                .name(serviceInstance.getServiceId())
                .payload(zookeeperInstance)
                .port(serviceInstance.getPort())
                .build();

        return registration;
    }
}
