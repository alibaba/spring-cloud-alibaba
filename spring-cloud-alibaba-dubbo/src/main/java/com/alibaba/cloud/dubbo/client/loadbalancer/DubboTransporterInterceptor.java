/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.dubbo.client.loadbalancer;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.dubbo.http.MutableHttpServerRequest;
import com.alibaba.cloud.dubbo.metadata.DubboRestServiceMetadata;
import com.alibaba.cloud.dubbo.metadata.RequestMetadata;
import com.alibaba.cloud.dubbo.metadata.RestMethodMetadata;
import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContext;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContextFactory;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceFactory;
import org.apache.dubbo.rpc.service.GenericException;
import org.apache.dubbo.rpc.service.GenericService;

import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.UriComponents;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

/**
 * Dubbo Transporter {@link ClientHttpRequestInterceptor} implementation.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoadBalancerInterceptor
 */
public class DubboTransporterInterceptor implements ClientHttpRequestInterceptor {

	private final DubboServiceMetadataRepository repository;

	private final DubboClientHttpResponseFactory clientHttpResponseFactory;

	private final Map<String, Object> dubboTranslatedAttributes;

	private final DubboGenericServiceFactory serviceFactory;

	private final DubboGenericServiceExecutionContextFactory contextFactory;

	private final PathMatcher pathMatcher = new AntPathMatcher();

	public DubboTransporterInterceptor(
			DubboServiceMetadataRepository dubboServiceMetadataRepository,
			List<HttpMessageConverter<?>> messageConverters, ClassLoader classLoader,
			Map<String, Object> dubboTranslatedAttributes,
			DubboGenericServiceFactory serviceFactory,
			DubboGenericServiceExecutionContextFactory contextFactory) {
		this.repository = dubboServiceMetadataRepository;
		this.dubboTranslatedAttributes = dubboTranslatedAttributes;
		this.clientHttpResponseFactory = new DubboClientHttpResponseFactory(
				messageConverters, classLoader);
		this.serviceFactory = serviceFactory;
		this.contextFactory = contextFactory;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {

		URI originalUri = request.getURI();

		String serviceName = originalUri.getHost();

		RequestMetadata clientMetadata = buildRequestMetadata(request);

		DubboRestServiceMetadata metadata = repository.get(serviceName, clientMetadata);

		if (metadata == null) {
			// if DubboServiceMetadata is not found, executes next
			return execution.execute(request, body);
		}

		RestMethodMetadata dubboRestMethodMetadata = metadata.getRestMethodMetadata();

		GenericService genericService = serviceFactory.create(metadata,
				dubboTranslatedAttributes);

		MutableHttpServerRequest httpServerRequest = new MutableHttpServerRequest(request,
				body);

		customizeRequest(httpServerRequest, dubboRestMethodMetadata, clientMetadata);

		DubboGenericServiceExecutionContext context = contextFactory
				.create(dubboRestMethodMetadata, httpServerRequest);

		Object result = null;
		GenericException exception = null;

		try {
			result = genericService.$invoke(context.getMethodName(),
					context.getParameterTypes(), context.getParameters());
		}
		catch (GenericException e) {
			exception = e;
		}

		return clientHttpResponseFactory.build(result, exception, clientMetadata,
				dubboRestMethodMetadata);
	}

	protected void customizeRequest(MutableHttpServerRequest httpServerRequest,
			RestMethodMetadata dubboRestMethodMetadata, RequestMetadata clientMetadata) {

		RequestMetadata dubboRequestMetadata = dubboRestMethodMetadata.getRequest();
		String pathPattern = dubboRequestMetadata.getPath();

		Map<String, String> pathVariables = pathMatcher
				.extractUriTemplateVariables(pathPattern, httpServerRequest.getPath());

		if (!CollectionUtils.isEmpty(pathVariables)) {
			// Put path variables Map into query parameters Map
			httpServerRequest.params(pathVariables);
		}

	}

	private RequestMetadata buildRequestMetadata(HttpRequest request) {
		UriComponents uriComponents = fromUri(request.getURI()).build(true);
		RequestMetadata requestMetadata = new RequestMetadata();
		requestMetadata.setPath(uriComponents.getPath());
		requestMetadata.setMethod(request.getMethod().name());
		requestMetadata.setParams(uriComponents.getQueryParams());
		requestMetadata.setHeaders(request.getHeaders());
		return requestMetadata;
	}

}
