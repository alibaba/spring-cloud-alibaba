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

import org.apache.dubbo.rpc.service.GenericService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * {@link DubboMetadataService} {@link InvocationHandler}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
class DubboMetadataServiceInvocationHandler implements InvocationHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GenericService genericService;

    public DubboMetadataServiceInvocationHandler(String serviceName, String version, DubboGenericServiceFactory dubboGenericServiceFactory) {
        this.genericService = dubboGenericServiceFactory.create(serviceName, DubboMetadataService.class, version);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object returnValue = null;
        try {
            returnValue = genericService.$invoke(method.getName(), getParameterTypes(method), args);
        } catch (Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
        }
        return returnValue;
    }

    private String[] getParameterTypes(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return Stream.of(parameterTypes).map(Class::getName).toArray(length -> new String[length]);
    }
}
