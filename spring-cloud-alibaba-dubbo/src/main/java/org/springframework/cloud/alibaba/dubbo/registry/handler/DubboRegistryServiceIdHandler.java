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
package org.springframework.cloud.alibaba.dubbo.registry.handler;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Dubbo {@link Registry} Spring Cloud Service Id Builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public interface DubboRegistryServiceIdHandler {

    /**
     * Supports the specified id of Spring Cloud Service or not
     *
     * @param serviceId the specified id of Spring Cloud Service
     * @return if supports, return <code>true</code>, or <code>false</code>
     */
    boolean supports(String serviceId);

    /**
     * Creates the id of Spring Cloud Service
     *
     * @param url The Dubbo's {@link URL}
     * @return non-null
     */
    String createServiceId(URL url);

    /**
     * The instance if {@link ConfigurableApplicationContext} .
     *
     * @return non-null
     */
    ConfigurableApplicationContext getContext();

}
