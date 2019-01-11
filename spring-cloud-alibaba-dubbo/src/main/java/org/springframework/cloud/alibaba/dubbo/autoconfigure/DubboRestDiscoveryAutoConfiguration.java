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

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.Map;

/**
 * The Auto-Configuration class for Dubbo REST Discovery
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration
@AutoConfigureAfter(value = {
        DubboRestAutoConfiguration.class,
        DubboRestMetadataRegistrationAutoConfiguration.class})
public class DubboRestDiscoveryAutoConfiguration {


    @Autowired
    private DiscoveryClient discoveryClient;

    // 1. Get all service names from Spring beans that was annotated by @FeignClient
    // 2. Get all service instances by echo specified service name
    // 3. Get Rest metadata from service instance
    // 4. Resolve REST metadata from the @FeignClient instance

    @Bean
    public SmartInitializingSingleton onBeansInitialized(ListableBeanFactory beanFactory) {
        return () -> {
            Map<String, Object> feignClientBeans = beanFactory.getBeansWithAnnotation(FeignClient.class);
            feignClientBeans.forEach((beanName, bean) -> {
                if (bean instanceof NamedContextFactory.Specification) {
                    NamedContextFactory.Specification specification = (NamedContextFactory.Specification) bean;
                    String serviceName = specification.getName();
                }
            });
        };
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed(ContextRefreshedEvent event) {

    }


}
