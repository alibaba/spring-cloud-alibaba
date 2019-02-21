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

import org.springframework.cloud.alibaba.dubbo.http.DefaultHttpServerRequest;
import org.springframework.cloud.alibaba.dubbo.metadata.DubboServiceMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.DubboTransportedMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RequestMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RestMethodMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceExecutionContext;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceExecutionContextFactory;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.util.UriComponents;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

/**
 * Dubbo {@link ClientHttpRequestInterceptor} implementation to adapt {@link LoadBalancerInterceptor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoadBalancerInterceptor
 */
public class DubboAdapterLoadBalancerInterceptor implements ClientHttpRequestInterceptor {

    private final DubboServiceMetadataRepository repository;

    private final LoadBalancerInterceptor loadBalancerInterceptor;

    private final DubboClientHttpResponseFactory clientHttpResponseFactory;

    private final DubboTransportedMetadata dubboTransportedMetadata;

    private final DubboGenericServiceFactory serviceFactory;

    private final DubboGenericServiceExecutionContextFactory contextFactory;

    public DubboAdapterLoadBalancerInterceptor(DubboServiceMetadataRepository dubboServiceMetadataRepository,
                                               LoadBalancerInterceptor loadBalancerInterceptor,
                                               List<HttpMessageConverter<?>> messageConverters,
                                               ClassLoader classLoader,
                                               DubboTransportedMetadata dubboTransportedMetadata,
                                               DubboGenericServiceFactory serviceFactory,
                                               DubboGenericServiceExecutionContextFactory contextFactory) {
        this.repository = dubboServiceMetadataRepository;
        this.loadBalancerInterceptor = loadBalancerInterceptor;
        this.dubboTransportedMetadata = dubboTransportedMetadata;
        this.clientHttpResponseFactory = new DubboClientHttpResponseFactory(messageConverters, classLoader);
        this.serviceFactory = serviceFactory;
        this.contextFactory = contextFactory;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        URI originalUri = request.getURI();

        String serviceName = originalUri.getHost();

        repository.initialize(serviceName);

        RequestMetadata clientMetadata = buildRequestMetadata(request);

        DubboServiceMetadata dubboServiceMetadata = repository.get(serviceName, clientMetadata);

        if (dubboServiceMetadata == null) { // if DubboServiceMetadata is not found
            return loadBalancerInterceptor.intercept(request, body, execution);
        }

        RestMethodMetadata dubboRestMethodMetadata = dubboServiceMetadata.getRestMethodMetadata();

        GenericService genericService = serviceFactory.create(dubboServiceMetadata, dubboTransportedMetadata);

        DubboGenericServiceExecutionContext context = contextFactory.create(dubboRestMethodMetadata,
                new DefaultHttpServerRequest(request, body));

        Object result = null;
        GenericException exception = null;

        try {
            result = genericService.$invoke(context.getMethodName(), context.getParameterTypes(), context.getParameters());
        } catch (GenericException e) {
            exception = e;
        }

        return clientHttpResponseFactory.build(result, exception, clientMetadata, dubboRestMethodMetadata);
    }

    public static RequestMetadata buildRequestMetadata(HttpRequest request) {
        UriComponents uriComponents = fromUri(request.getURI()).build(true);
        RequestMetadata requestMetadata = new RequestMetadata();
        requestMetadata.setPath(uriComponents.getPath());
        requestMetadata.setMethod(request.getMethod().name());
        requestMetadata.setParams(uriComponents.getQueryParams());
        requestMetadata.setHeaders(request.getHeaders());
        return requestMetadata;
    }
}
