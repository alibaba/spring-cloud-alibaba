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
package com.alibaba.cloud.dubbo.gateway;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.cloud.dubbo.http.MutableHttpServerRequest;
import com.alibaba.cloud.dubbo.metadata.DubboRestServiceMetadata;
import com.alibaba.cloud.dubbo.metadata.RequestMetadata;
import com.alibaba.cloud.dubbo.metadata.RestMethodMetadata;
import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContext;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContextFactory;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.dubbo.rpc.service.GenericException;
import org.apache.dubbo.rpc.service.GenericService;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.HttpRequest;

import static com.alibaba.cloud.dubbo.http.util.HttpUtils.getParameters;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.apache.dubbo.common.constants.CommonConstants.PATH_SEPARATOR;

/**
 * The executor of Dubbo Cloud Gateway that handles the HTTP request and responses the
 * result of execution of the generic invocation to the Dubbo service providers
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * 
 */
public class DubboCloudGatewayExecutor {

	private final Log logger = LogFactory.getLog(getClass());

	private final DubboServiceMetadataRepository repository;

	private final DubboGenericServiceFactory serviceFactory;

	private final DubboGenericServiceExecutionContextFactory contextFactory;

	private final DubboCloudGatewayProperties dubboCloudGatewayProperties;

	private final ConversionService conversionService;

	private final Map<String, Object> dubboTranslatedAttributes = new HashMap<>();

	public DubboCloudGatewayExecutor(DubboServiceMetadataRepository repository,
			DubboGenericServiceFactory serviceFactory,
			DubboGenericServiceExecutionContextFactory contextFactory,
			DubboCloudGatewayProperties dubboCloudGatewayProperties,
			ObjectProvider<ConversionService> conversionServices) {
		this.repository = repository;
		this.serviceFactory = serviceFactory;
		this.contextFactory = contextFactory;
		this.dubboCloudGatewayProperties = dubboCloudGatewayProperties;
		this.conversionService = conversionServices
				.getIfAvailable(DefaultFormattingConversionService::new);
		// TODO : Replace these hard-code configurations
		this.dubboTranslatedAttributes.put("protocol", "dubbo");
		this.dubboTranslatedAttributes.put("cluster", "failover");
	}

	public Object execute(HttpRequest request) {

		String serviceName = resolveServiceName(request);

		String restPath = substringAfter(request.getURI().getPath(), serviceName);

		// 初始化 serviceName 的 REST 请求元数据
		repository.initializeMetadata(serviceName);
		// 将 HttpServletRequest 转化为 RequestMetadata
		RequestMetadata clientMetadata = buildRequestMetadata(request, restPath);

		DubboRestServiceMetadata dubboRestServiceMetadata = repository.get(serviceName,
				clientMetadata);

		Object result = null;

		if (dubboRestServiceMetadata != null) {

			RestMethodMetadata dubboRestMethodMetadata = dubboRestServiceMetadata
					.getRestMethodMetadata();

			GenericService genericService = serviceFactory
					.create(dubboRestServiceMetadata, dubboTranslatedAttributes);

			byte[] body = getRequestBody(request);

			MutableHttpServerRequest httpServerRequest = new MutableHttpServerRequest(
					request, body);

			DubboGenericServiceExecutionContext context = contextFactory
					.create(dubboRestMethodMetadata, httpServerRequest);

			GenericException exception = null;

			try {
				result = genericService.$invoke(context.getMethodName(),
						context.getParameterTypes(), context.getParameters());

				String returnType = dubboRestMethodMetadata.getReturnType();

				logger.info("The result is " + result);

			}
			catch (GenericException e) {
				exception = e;
			}
		}

		return result;

	}

	private String resolveServiceName(HttpRequest request) {
		URI uri = request.getURI();
		String requestURI = uri.getPath();
		String servletPath = dubboCloudGatewayProperties.getContextPath();
		String part = substringAfter(requestURI, servletPath);
		String serviceName = substringBetween(part, PATH_SEPARATOR, PATH_SEPARATOR);
		return serviceName;
	}

	/**
	 * TODO : Get the Request Body from HttpRequest
	 * 
	 * @param request {@link HttpRequest}
	 * @return
	 */
	private byte[] getRequestBody(HttpRequest request) {
		return new byte[0];
	}

	private RequestMetadata buildRequestMetadata(HttpRequest request, String restPath) {
		RequestMetadata requestMetadata = new RequestMetadata();
		requestMetadata.setPath(restPath);
		requestMetadata.setMethod(request.getMethod().toString());
		requestMetadata.setParams(getParameters(request));
		requestMetadata.setHeaders(request.getHeaders());
		return requestMetadata;
	}

}
