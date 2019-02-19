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
package org.springframework.cloud.alibaba.dubbo.metadata.resolver;

import org.springframework.cloud.alibaba.dubbo.metadata.MethodMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.MethodParameterMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RequestMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RestMethodMetadata;
import org.springframework.http.HttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponents;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parameter Resolver
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class ParameterResolver {


    public Object[] resolveParameters(RestMethodMetadata restMethodMetadata, HttpRequest request, UriComponents uriComponents) {

        MethodMetadata methodMetadata = restMethodMetadata.getMethod();

        RequestMetadata requestMetadata = restMethodMetadata.getRequest();

        Map<Integer, Collection<String>> indexToName = restMethodMetadata.getIndexToName();

        List<MethodParameterMetadata> params = methodMetadata.getParams();

        Object[] parameters = new Object[params.size()];

        for (MethodParameterMetadata parameterMetadata : params) {

            int index = parameterMetadata.getIndex();

            String name = getName(indexToName, index);

            parameters[index] = getValue(requestMetadata, request, uriComponents, name);

        }

        return parameters;
    }

    private String getValue(RequestMetadata requestMetadata, HttpRequest request, UriComponents uriComponents, String name) {

        String value = null;
        Set<String> paramNames = requestMetadata.getParamNames();
        Set<String> headerNames = requestMetadata.getHeaderNames();

        if (paramNames.contains(name)) {
            value = uriComponents.getQueryParams().getFirst(name);
        } else if (headerNames.contains(name)) {
            value = request.getHeaders().getFirst(name);
        }

        return value;
    }

    private String getName(Map<Integer, Collection<String>> indexToName, int index) {
        Collection<String> names = indexToName.get(index);
        String name = null;
        if (!CollectionUtils.isEmpty(names)) {
            Iterator<String> iterator = names.iterator();
            while (iterator.hasNext()) {
                name = iterator.next(); // choose the last one if more than one
            }
        }
        return name;
    }

    public String[] resolveParameterTypes(MethodMetadata methodMetadata) {

        List<MethodParameterMetadata> params = methodMetadata.getParams();

        String[] parameterTypes = new String[params.size()];

        for (MethodParameterMetadata parameterMetadata : params) {
            int index = parameterMetadata.getIndex();
            String parameterType = parameterMetadata.getType();
            parameterTypes[index] = parameterType;
        }

        return parameterTypes;
    }
}
