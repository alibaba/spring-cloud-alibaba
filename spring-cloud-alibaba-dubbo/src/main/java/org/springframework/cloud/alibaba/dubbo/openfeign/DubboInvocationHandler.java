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
package org.springframework.cloud.alibaba.dubbo.openfeign;

import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.cloud.alibaba.dubbo.metadata.RestMethodMetadata;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceExecutionContext;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceExecutionContextFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Dubbo {@link GenericService} for {@link InvocationHandler}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboInvocationHandler implements InvocationHandler {

    private final Map<Method, FeignMethodMetadata> feignMethodMetadataMap;

    private final InvocationHandler defaultInvocationHandler;

    private final DubboGenericServiceExecutionContextFactory contextFactory;

    public DubboInvocationHandler(Map<Method, FeignMethodMetadata> feignMethodMetadataMap,
                                  InvocationHandler defaultInvocationHandler,
                                  DubboGenericServiceExecutionContextFactory contextFactory) {
        this.feignMethodMetadataMap = feignMethodMetadataMap;
        this.defaultInvocationHandler = defaultInvocationHandler;
        this.contextFactory = contextFactory;
    }

    @Override
    public Object invoke(Object proxy, Method feignMethod, Object[] args) throws Throwable {

        FeignMethodMetadata feignMethodMetadata = feignMethodMetadataMap.get(feignMethod);

        if (feignMethodMetadata == null) {
            return defaultInvocationHandler.invoke(proxy, feignMethod, args);
        }

        GenericService dubboGenericService = feignMethodMetadata.getDubboGenericService();
        RestMethodMetadata dubboRestMethodMetadata = feignMethodMetadata.getDubboRestMethodMetadata();
        RestMethodMetadata feignRestMethodMetadata = feignMethodMetadata.getFeignMethodMetadata();

        DubboGenericServiceExecutionContext context = contextFactory.create(dubboRestMethodMetadata, feignRestMethodMetadata, args);

        String methodName = context.getMethodName();
        String[] parameterTypes = context.getParameterTypes();
        Object[] parameters = context.getParameters();

        return dubboGenericService.$invoke(methodName, parameterTypes, parameters);
    }
}
