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
package org.springframework.cloud.alibaba.dubbo.service;

import org.springframework.beans.factory.BeanClassLoaderAware;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * The proxy of {@link DubboMetadataConfigService}
 */
public class DubboMetadataConfigServiceProxy implements BeanClassLoaderAware {

    private final DubboGenericServiceFactory dubboGenericServiceFactory;

    private ClassLoader classLoader;

    public DubboMetadataConfigServiceProxy(DubboGenericServiceFactory dubboGenericServiceFactory) {
        this.dubboGenericServiceFactory = dubboGenericServiceFactory;
    }

    /**
     * New proxy instance of {@link DubboMetadataConfigService} via the specified service name
     *
     * @param serviceName the service name
     * @return a {@link DubboMetadataConfigService} proxy
     */
    public DubboMetadataConfigService newProxy(String serviceName) {
        return (DubboMetadataConfigService) newProxyInstance(classLoader, new Class[]{DubboMetadataConfigService.class},
                new DubboMetadataConfigServiceInvocationHandler(serviceName, dubboGenericServiceFactory));
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
