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
package org.springframework.cloud.alibaba.dubbo.service.parameter;

import org.springframework.cloud.alibaba.dubbo.metadata.MethodParameterMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RestMethodMetadata;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

/**
 * HTTP Request Parameter {@link DubboGenericServiceParameterResolver Dubbo GenericService Parameter Resolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class RequestParamServiceParameterResolver extends AbstractDubboGenericServiceParameterResolver {

    public static final int DEFAULT_ORDER = 1;

    public RequestParamServiceParameterResolver() {
        super();
        setOrder(DEFAULT_ORDER);
    }

    @Override
    public boolean supportParameter(RestMethodMetadata restMethodMetadata, MethodParameterMetadata methodParameterMetadata) {
        Map<Integer, Collection<String>> indexToName = restMethodMetadata.getIndexToName();

        int index = methodParameterMetadata.getIndex();

        Collection<String> paramNames = indexToName.get(index);

        if (CollectionUtils.isEmpty(paramNames)) {
            return false;
        }

        String paramName = methodParameterMetadata.getName();

        return paramNames.contains(paramName);
    }

    @Override
    public Object resolveParameter(RestMethodMetadata restMethodMetadata, MethodParameterMetadata parameterMetadata,
                                   ServerHttpRequest request) {

        URI uri = request.getURI();

        UriComponents uriComponents = fromUri(uri).build(true);

        MultiValueMap<String, String> params = uriComponents.getQueryParams();

        String paramName = parameterMetadata.getName();

        Class<?> parameterType = resolveClass(parameterMetadata.getType());

        Object paramValue = null;

        if (parameterType.isArray()) { // Array type
            paramValue = params.get(paramName);
        } else {
            paramValue = params.getFirst(paramName);
        }

        return resolveValue(paramValue, parameterType);
    }
}
