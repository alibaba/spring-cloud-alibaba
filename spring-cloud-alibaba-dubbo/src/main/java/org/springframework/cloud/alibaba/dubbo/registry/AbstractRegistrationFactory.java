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

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.springframework.cloud.alibaba.dubbo.registry.handler.DubboRegistryServiceIdHandler;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.LinkedHashMap;

/**
 * Abstract {@link RegistrationFactory} implementation
 * <p>
 *
 * @param <R> The subclass of {@link Registration}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public abstract class AbstractRegistrationFactory<R extends Registration> implements RegistrationFactory<R> {

    public final R create(URL url, ConfigurableApplicationContext applicationContext) {
        ServiceInstance serviceInstance = createServiceInstance(url, applicationContext);
        return create(serviceInstance, applicationContext);
    }

    /**
     * Create an instance {@link ServiceInstance}. This method maybe override by sub-class.
     *
     * @param url                The Dubbo's {@link URL}
     * @param applicationContext {@link ConfigurableApplicationContext}
     * @return an instance {@link ServiceInstance}
     */
    protected ServiceInstance createServiceInstance(URL url, ConfigurableApplicationContext applicationContext) {
        String serviceId = createServiceId(url, applicationContext);
        // Append default category if absent
        String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
        URL newURL = url.addParameter(Constants.CATEGORY_KEY, category);
        newURL = newURL.addParameter(Constants.PROTOCOL_KEY, url.getProtocol());
        String ip = url.getIp();
        int port = newURL.getParameter(Constants.BIND_PORT_KEY, url.getPort());
        DefaultServiceInstance serviceInstance = new DefaultServiceInstance(url.toIdentityString(), serviceId, ip, port, false);
        serviceInstance.getMetadata().putAll(new LinkedHashMap<>(newURL.getParameters()));
        return serviceInstance;
    }

    protected String createServiceId(URL url, ConfigurableApplicationContext applicationContext) {
        DubboRegistryServiceIdHandler handler = applicationContext.getBean(DubboRegistryServiceIdHandler.class);
        return handler.createServiceId(url);
    }
}

