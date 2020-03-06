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

package com.alibaba.cloud.dubbo.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.util.MultiValueMap;

import static com.alibaba.cloud.dubbo.http.util.HttpUtils.getParameters;

/**
 * Mutable {@link HttpServerRequest} implementation.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class MutableHttpServerRequest implements HttpServerRequest {

	private final HttpMethod httpMethod;

	private final URI uri;

	private final String path;

	private final MultiValueMap<String, String> queryParams;

	private final HttpHeaders httpHeaders;

	private final HttpInputMessage httpInputMessage;

	public MutableHttpServerRequest(HttpRequest httpRequest, byte[] body) {
		this.httpMethod = httpRequest.getMethod();
		this.uri = httpRequest.getURI();
		this.path = uri.getPath();
		this.httpHeaders = httpRequest.getHeaders();
		this.queryParams = getParameters(httpRequest);
		this.httpInputMessage = new ByteArrayHttpInputMessage(body);
	}

	public MutableHttpServerRequest params(Map<String, String> params) {
		queryParams.setAll(params);
		return this;
	}

	@Override
	public InputStream getBody() throws IOException {
		return httpInputMessage.getBody();
	}

	@Override
	public HttpMethod getMethod() {
		return httpMethod;
	}

	// Override method since Spring Framework 5.0
	@Override
	public String getMethodValue() {
		return httpMethod.name();
	}

	@Override
	public URI getURI() {
		return uri;
	}

	@Override
	public HttpHeaders getHeaders() {
		return httpHeaders;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public MultiValueMap<String, String> getQueryParams() {
		return queryParams;
	}

}
