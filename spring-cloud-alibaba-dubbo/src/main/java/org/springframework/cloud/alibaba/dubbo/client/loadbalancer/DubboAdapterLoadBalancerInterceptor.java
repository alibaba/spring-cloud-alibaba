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
package org.springframework.cloud.alibaba.dubbo.client.loadbalancer;

import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.service.GenericService;

import org.springframework.cloud.alibaba.dubbo.metadata.DubboServiceMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.DubboTransportedMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.MethodMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RequestMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RestMethodMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.alibaba.dubbo.metadata.resolver.ParameterResolver;
import org.springframework.cloud.alibaba.dubbo.metadata.service.DubboGenericServiceFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Dubbo {@link ClientHttpRequestInterceptor} implementation to adapt {@link LoadBalancerInterceptor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoadBalancerInterceptor
 */
public class DubboAdapterLoadBalancerInterceptor implements ClientHttpRequestInterceptor {

    private final ParameterResolver parameterResolver = new ParameterResolver();

    private final DubboServiceMetadataRepository repository;

    private final LoadBalancerInterceptor loadBalancerInterceptor;

    private final DubboClientHttpResponseFactory clientHttpResponseFactory;

    private final DubboTransportedMetadata dubboTransportedMetadata;

    private final DubboGenericServiceFactory dubboGenericServiceFactory;

    public DubboAdapterLoadBalancerInterceptor(DubboServiceMetadataRepository dubboServiceMetadataRepository,
                                               LoadBalancerInterceptor loadBalancerInterceptor,
                                               List<HttpMessageConverter<?>> messageConverters,
                                               ClassLoader classLoader,
                                               DubboTransportedMetadata dubboTransportedMetadata,
                                               DubboGenericServiceFactory dubboGenericServiceFactory) {
        this.repository = dubboServiceMetadataRepository;
        this.loadBalancerInterceptor = loadBalancerInterceptor;
        this.dubboTransportedMetadata = dubboTransportedMetadata;
        this.clientHttpResponseFactory = new DubboClientHttpResponseFactory(messageConverters, classLoader);
        this.dubboGenericServiceFactory = dubboGenericServiceFactory;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        URI originalUri = request.getURI();

        UriComponents uriComponents = UriComponentsBuilder.fromUri(originalUri).build(true);

        String serviceName = originalUri.getHost();

        repository.initialize(serviceName);

        RequestMetadata clientMetadata = buildRequestMetadata(request, uriComponents);

        DubboServiceMetadata dubboServiceMetadata = repository.get(serviceName, clientMetadata);

        if (dubboServiceMetadata == null) { // if DubboServiceMetadata is not found
            return loadBalancerInterceptor.intercept(request, body, execution);
        }

        RestMethodMetadata restMethodMetadata = dubboServiceMetadata.getRestMethodMetadata();

        GenericService genericService = dubboGenericServiceFactory.create(dubboServiceMetadata, dubboTransportedMetadata);

        Object result = null;
        GenericException exception = null;

        try {
            result = invokeService(restMethodMetadata, genericService, clientMetadata);
        } catch (GenericException e) {
            exception = e;
        }

        return clientHttpResponseFactory.build(result, exception, clientMetadata, restMethodMetadata);
    }

    private Object invokeService(RestMethodMetadata restMethodMetadata, GenericService genericService,
                                 RequestMetadata clientMetadata) throws GenericException {

        MethodMetadata methodMetadata = restMethodMetadata.getMethod();

        String methodName = methodMetadata.getName();

        String[] parameterTypes = parameterResolver.resolveParameterTypes(methodMetadata);

        Object[] parameters = parameterResolver.resolveParameters(restMethodMetadata, clientMetadata);

        Object result = genericService.$invoke(methodName, parameterTypes, parameters);

        return result;
    }

    public static RequestMetadata buildRequestMetadata(HttpRequest request, UriComponents uriComponents) {
        RequestMetadata requestMetadata = new RequestMetadata();
        requestMetadata.setPath(uriComponents.getPath());
        requestMetadata.setMethod(request.getMethod().name());
        requestMetadata.setParams(uriComponents.getQueryParams());
        requestMetadata.setHeaders(request.getHeaders());
        return requestMetadata;
    }
}
