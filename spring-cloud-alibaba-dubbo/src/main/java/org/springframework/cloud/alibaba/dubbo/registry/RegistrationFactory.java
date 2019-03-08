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
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * {@link Registration} Factory to createServiceInstance a instance of {@link Registration}
 *
 * @param <R> The subclass of {@link Registration}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public interface RegistrationFactory<R extends Registration> {

    /**
     * Create a instance of {@link R}
     *
     * @param url                The Dubbo's {@link URL}
     * @param applicationContext {@link ConfigurableApplicationContext}
     * @return a instance of {@link R}, if null, it indicates the registration will not be executed.
     */
    R create(URL url, ConfigurableApplicationContext applicationContext);

    /**
     * Create a instance of {@link R}
     *
     * @param serviceInstance    {@link ServiceInstance}
     * @param applicationContext {@link ConfigurableApplicationContext}
     * @return a instance of {@link R}, if null, it indicates the registration will not be executed.
     */
    R create(ServiceInstance serviceInstance, ConfigurableApplicationContext applicationContext);
}
