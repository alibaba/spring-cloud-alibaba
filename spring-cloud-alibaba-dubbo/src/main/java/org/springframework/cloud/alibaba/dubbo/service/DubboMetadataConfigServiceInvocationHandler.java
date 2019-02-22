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

import com.alibaba.dubbo.rpc.service.GenericService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * {@link DubboMetadataConfigService} {@link InvocationHandler}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
class DubboMetadataConfigServiceInvocationHandler implements InvocationHandler {

    /**
     * The method name of {@link DubboMetadataConfigService#getServiceRestMetadata()}
     */
    private static final String METHOD_NAME = "getServiceRestMetadata";

    private static final String[] PARAMETER_TYPES = new String[0];

    private static final String[] PARAMETER_VALUES = new String[0];

    private final GenericService genericService;

    public DubboMetadataConfigServiceInvocationHandler(String serviceName, DubboGenericServiceFactory dubboGenericServiceFactory) {
        this.genericService = dubboGenericServiceFactory.create(serviceName, DubboMetadataConfigService.class);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (METHOD_NAME.equals(methodName)) {
            return genericService.$invoke(methodName, PARAMETER_TYPES, PARAMETER_VALUES);
        }
        return method.invoke(proxy, args);
    }
}
