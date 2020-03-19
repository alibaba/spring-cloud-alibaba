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
import java.util.List;

import com.alibaba.cloud.dubbo.http.converter.HttpMessageConverterHolder;
import com.alibaba.cloud.dubbo.http.util.HttpMessageConverterResolver;
import com.alibaba.cloud.dubbo.metadata.RequestMetadata;
import com.alibaba.cloud.dubbo.metadata.RestMethodMetadata;
import org.apache.dubbo.rpc.service.GenericException;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * Dubbo {@link ClientHttpResponse} Factory.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
class DubboClientHttpResponseFactory {

	private final HttpMessageConverterResolver httpMessageConverterResolver;

	DubboClientHttpResponseFactory(List<HttpMessageConverter<?>> messageConverters,
			ClassLoader classLoader) {
		this.httpMessageConverterResolver = new HttpMessageConverterResolver(
				messageConverters, classLoader);
	}

	public ClientHttpResponse build(Object result, GenericException exception,
			RequestMetadata requestMetadata, RestMethodMetadata restMethodMetadata) {

		DubboHttpOutputMessage httpOutputMessage = new DubboHttpOutputMessage();

		HttpMessageConverterHolder httpMessageConverterHolder = httpMessageConverterResolver
				.resolve(requestMetadata, restMethodMetadata);

		if (httpMessageConverterHolder != null) {
			MediaType mediaType = httpMessageConverterHolder.getMediaType();
			HttpMessageConverter converter = httpMessageConverterHolder.getConverter();
			try {
				converter.write(result, mediaType, httpOutputMessage);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		return new DubboClientHttpResponse(httpOutputMessage, exception);
	}

}
