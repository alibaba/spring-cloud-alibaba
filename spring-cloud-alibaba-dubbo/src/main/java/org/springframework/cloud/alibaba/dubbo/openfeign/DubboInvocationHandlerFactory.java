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

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.rpc.service.GenericService;

import feign.Contract;
import feign.InvocationHandlerFactory;
import feign.MethodMetadata;
import feign.Target;
import org.springframework.cloud.alibaba.dubbo.metadata.RequestMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static feign.Feign.configKey;

/**
 * Dubbo {@link InvocationHandlerFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboInvocationHandlerFactory implements InvocationHandlerFactory {

    private final static InvocationHandlerFactory DEFAULT_INVOCATION_HANDLER_FACTORY =
            new InvocationHandlerFactory.Default();

    private final Contract contract;

    private final DubboServiceMetadataRepository dubboServiceRepository;

    public DubboInvocationHandlerFactory(Contract contract, DubboServiceMetadataRepository dubboServiceRepository) {
        this.contract = contract;
        this.dubboServiceRepository = dubboServiceRepository;
    }

    @Override
    public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
        // The target class annotated @FeignClient
        Class<?> targetType = target.type();
        // Resolve metadata from current @FeignClient type
        Map<Method, RequestMetadata> methodRequestMetadataMap = resolveMethodRequestMetadataMap(targetType, dispatch.keySet());
        // @FeignClient specifies the service name
        String serviceName = target.name();
        // Update specified metadata
        dubboServiceRepository.updateMetadata(serviceName);

        Map<Method, GenericService> genericServicesMap = new HashMap<>();

        Map<Method, org.springframework.cloud.alibaba.dubbo.metadata.MethodMetadata> methodMetadataMap = new HashMap<>();

        methodRequestMetadataMap.forEach((method, requestMetadata) -> {
            ReferenceBean<GenericService> referenceBean = dubboServiceRepository.getReferenceBean(serviceName, requestMetadata);
            org.springframework.cloud.alibaba.dubbo.metadata.MethodMetadata methodMetadata =
                    dubboServiceRepository.getMethodMetadata(serviceName, requestMetadata);
            genericServicesMap.put(method, referenceBean.get());
            methodMetadataMap.put(method, methodMetadata);
        });

        InvocationHandler defaultInvocationHandler = DEFAULT_INVOCATION_HANDLER_FACTORY.create(target, dispatch);

        DubboInvocationHandler invocationHandler = new DubboInvocationHandler(genericServicesMap, methodMetadataMap,
                defaultInvocationHandler);

        return invocationHandler;
    }

    private Map<Method, RequestMetadata> resolveMethodRequestMetadataMap(Class<?> targetType, Set<Method> methods) {
        Map<String, RequestMetadata> requestMetadataMap = resolveRequestMetadataMap(targetType);
        return methods.stream().collect(Collectors.toMap(method -> method, method ->
                requestMetadataMap.get(configKey(targetType, method))
        ));
    }

    private Map<String, RequestMetadata> resolveRequestMetadataMap(Class<?> targetType) {
        return contract.parseAndValidatateMetadata(targetType)
                .stream().collect(Collectors.toMap(MethodMetadata::configKey, this::requestMetadata));
    }

    private RequestMetadata requestMetadata(MethodMetadata methodMetadata) {
        return new RequestMetadata(methodMetadata.template());
    }
}
