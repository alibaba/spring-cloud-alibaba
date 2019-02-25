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
import org.springframework.cloud.alibaba.dubbo.http.HttpServerRequest;
import org.springframework.cloud.alibaba.dubbo.metadata.MethodMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.MethodParameterMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RestMethodMetadata;
import org.springframework.cloud.alibaba.dubbo.service.parameter.DubboGenericServiceParameterResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public DubboGenericServiceExecutionContext create(RestMethodMetadata dubboRestMethodMetadata,
                                                      RestMethodMetadata clientMethodMetadata, Object[] arguments) {

        MethodMetadata dubboMethodMetadata = dubboRestMethodMetadata.getMethod();

        String methodName = dubboMethodMetadata.getName();

        String[] parameterTypes = resolveParameterTypes(dubboMethodMetadata);

        Object[] parameters = resolveParameters(dubboRestMethodMetadata, clientMethodMetadata, arguments);

        return new DubboGenericServiceExecutionContext(methodName, parameterTypes, parameters);
    }

    public DubboGenericServiceExecutionContext create(RestMethodMetadata dubboRestMethodMetadata,
                                                      HttpServerRequest request) {
        MethodMetadata methodMetadata = dubboRestMethodMetadata.getMethod();

        String methodName = methodMetadata.getName();

        String[] parameterTypes = resolveParameterTypes(methodMetadata);

        Object[] parameters = resolveParameters(dubboRestMethodMetadata, request);

        return new DubboGenericServiceExecutionContext(methodName, parameterTypes, parameters);
    }

    private Map<String, Integer> buildParamNameToIndex(List<MethodParameterMetadata> params) {
        Map<String, Integer> paramNameToIndex = new LinkedHashMap<>();
        for (MethodParameterMetadata param : params) {
            paramNameToIndex.put(param.getName(), param.getIndex());
        }
        return paramNameToIndex;
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

    protected Object[] resolveParameters(RestMethodMetadata dubboRestMethodMetadata, HttpServerRequest request) {

        MethodMetadata dubboMethodMetadata = dubboRestMethodMetadata.getMethod();

        List<MethodParameterMetadata> params = dubboMethodMetadata.getParams();

        Object[] parameters = new Object[params.size()];

        for (MethodParameterMetadata parameterMetadata : params) {

            int index = parameterMetadata.getIndex();

            for (DubboGenericServiceParameterResolver resolver : resolvers) {
                Object parameter = resolver.resolve(dubboRestMethodMetadata, parameterMetadata, request);
                if (parameter != null) {
                    parameters[index] = parameter;
                    break;
                }
            }
        }

        return parameters;
    }

    protected Object[] resolveParameters(RestMethodMetadata dubboRestMethodMetadata,
                                         RestMethodMetadata clientRestMethodMetadata, Object[] arguments) {

        MethodMetadata dubboMethodMetadata = dubboRestMethodMetadata.getMethod();

        List<MethodParameterMetadata> params = dubboMethodMetadata.getParams();

        Object[] parameters = new Object[params.size()];

        for (MethodParameterMetadata parameterMetadata : params) {

            int index = parameterMetadata.getIndex();

            for (DubboGenericServiceParameterResolver resolver : resolvers) {
                Object parameter = resolver.resolve(dubboRestMethodMetadata, parameterMetadata, clientRestMethodMetadata, arguments);
                if (parameter != null) {
                    parameters[index] = parameter;
                    break;
                }
            }
        }

        return parameters;
    }
}
