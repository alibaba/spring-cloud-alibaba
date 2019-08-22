/*
 * Copyright (C) 2018 the original author or authors.
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
package com.alibaba.cloud.dubbo.service.parameter;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.alibaba.cloud.dubbo.http.HttpServerRequest;
import com.alibaba.cloud.dubbo.http.converter.HttpMessageConverterHolder;
import com.alibaba.cloud.dubbo.http.util.HttpMessageConverterResolver;
import com.alibaba.cloud.dubbo.metadata.MethodParameterMetadata;
import com.alibaba.cloud.dubbo.metadata.RestMethodMetadata;

/**
 * HTTP Request Body {@link DubboGenericServiceParameterResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class RequestBodyServiceParameterResolver
		extends AbstractDubboGenericServiceParameterResolver {

	public static final int DEFAULT_ORDER = 7;

	@Autowired
	private ObjectProvider<HttpMessageConverters> httpMessageConverters;

	private HttpMessageConverterResolver httpMessageConverterResolver;

	public RequestBodyServiceParameterResolver() {
		super();
		setOrder(DEFAULT_ORDER);
	}

	@PostConstruct
	public void init() {
		HttpMessageConverters httpMessageConverters = this.httpMessageConverters
				.getIfAvailable();

		httpMessageConverterResolver = new HttpMessageConverterResolver(
				httpMessageConverters == null ? Collections.emptyList()
						: httpMessageConverters.getConverters(),
				getClassLoader());
	}

	private boolean supportParameter(RestMethodMetadata restMethodMetadata,
			MethodParameterMetadata methodParameterMetadata) {

		Integer index = methodParameterMetadata.getIndex();

		Integer bodyIndex = restMethodMetadata.getBodyIndex();

		if (!Objects.equals(index, bodyIndex)) {
			return false;
		}

		Class<?> parameterType = resolveClass(methodParameterMetadata.getType());

		Class<?> bodyType = resolveClass(restMethodMetadata.getBodyType());

		return Objects.equals(parameterType, bodyType);
	}

	@Override
	public Object resolve(RestMethodMetadata restMethodMetadata,
			MethodParameterMetadata methodParameterMetadata, HttpServerRequest request) {

		if (!supportParameter(restMethodMetadata, methodParameterMetadata)) {
			return null;
		}

		Object result = null;

		Class<?> parameterType = resolveClass(methodParameterMetadata.getType());

		HttpMessageConverterHolder holder = httpMessageConverterResolver.resolve(request,
				parameterType);

		if (holder != null) {
			HttpMessageConverter converter = holder.getConverter();
			try {
				result = converter.read(parameterType, request);
			}
			catch (IOException e) {
				throw new HttpMessageNotReadableException(
						"I/O error while reading input message", e);
			}
		}

		return result;
	}

	@Override
	public Object resolve(RestMethodMetadata restMethodMetadata,
			MethodParameterMetadata methodParameterMetadata,
			RestMethodMetadata clientRestMethodMetadata, Object[] arguments) {

		if (!supportParameter(restMethodMetadata, methodParameterMetadata)) {
			return null;
		}

		Integer clientBodyIndex = clientRestMethodMetadata.getBodyIndex();
		return arguments[clientBodyIndex];
	}
}
