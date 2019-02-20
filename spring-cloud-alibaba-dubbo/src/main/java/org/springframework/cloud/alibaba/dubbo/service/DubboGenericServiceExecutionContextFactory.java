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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.dubbo.metadata.MethodMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.MethodParameterMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RestMethodMetadata;
import org.springframework.cloud.alibaba.dubbo.service.parameter.DubboGenericServiceParameterResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.server.ServerHttpRequest;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link DubboGenericServiceExecutionContext} Factory
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DubboGenericServiceParameterResolver
 */
public class DubboGenericServiceExecutionContextFactory {

    @Autowired(required = false)
    private final List<DubboGenericServiceParameterResolver> resolvers = Collections.emptyList();

    @PostConstruct
    public void init() {
        AnnotationAwareOrderComparator.sort(resolvers);
    }

    public DubboGenericServiceExecutionContext create(RestMethodMetadata restMethodMetadata, Object[] arguments) {

        MethodMetadata methodMetadata = restMethodMetadata.getMethod();

        String methodName = methodMetadata.getName();

        String[] parameterTypes = resolveParameterTypes(methodMetadata);

        Object[] parameters = Arrays.copyOf(arguments, parameterTypes.length);

        return new DubboGenericServiceExecutionContext(methodName, parameterTypes, parameters);
    }


    public DubboGenericServiceExecutionContext create(RestMethodMetadata restMethodMetadata,
                                                      ServerHttpRequest request) {
        MethodMetadata methodMetadata = restMethodMetadata.getMethod();

        String methodName = methodMetadata.getName();

        String[] parameterTypes = resolveParameterTypes(methodMetadata);

        Object[] parameters = resolveParameters(restMethodMetadata, request);

        return new DubboGenericServiceExecutionContext(methodName, parameterTypes, parameters);
    }

    protected String[] resolveParameterTypes(MethodMetadata methodMetadata) {

        List<MethodParameterMetadata> params = methodMetadata.getParams();

        String[] parameterTypes = new String[params.size()];

        for (MethodParameterMetadata parameterMetadata : params) {
            int index = parameterMetadata.getIndex();
            String parameterType = parameterMetadata.getType();
            parameterTypes[index] = parameterType;
        }

        return parameterTypes;
    }

    protected Object[] resolveParameters(RestMethodMetadata restMethodMetadata, ServerHttpRequest request) {

        MethodMetadata methodMetadata = restMethodMetadata.getMethod();

        List<MethodParameterMetadata> params = methodMetadata.getParams();

        Object[] parameters = new Object[params.size()];

        for (MethodParameterMetadata parameterMetadata : params) {

            int index = parameterMetadata.getIndex();

            for (DubboGenericServiceParameterResolver resolver : resolvers) {
                if (resolver.supportParameter(restMethodMetadata, parameterMetadata)) {
                    parameters[index] = resolver.resolveParameter(restMethodMetadata, parameterMetadata, request);
                    break;
                }
            }
        }

        return parameters;
    }
}
