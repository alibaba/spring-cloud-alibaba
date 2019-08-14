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
package com.alibaba.cloud.dubbo.client.loadbalancer;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;

/**
 * Dubbo Metadata {@link ClientHttpRequestInterceptor} Initializing Interceptor executes
 * intercept before {@link DubboTransporterInterceptor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboMetadataInitializerInterceptor implements ClientHttpRequestInterceptor {

	private final DubboServiceMetadataRepository repository;

	public DubboMetadataInitializerInterceptor(
			DubboServiceMetadataRepository repository) {
		this.repository = repository;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {

		URI originalUri = request.getURI();

		String serviceName = originalUri.getHost();

		repository.initializeMetadata(serviceName);

		// Execute next
		return execution.execute(request, body);
	}
}
