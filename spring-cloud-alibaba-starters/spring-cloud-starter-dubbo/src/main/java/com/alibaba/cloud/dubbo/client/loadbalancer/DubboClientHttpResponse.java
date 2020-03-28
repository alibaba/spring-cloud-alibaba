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
import java.io.InputStream;

import org.apache.dubbo.rpc.service.GenericException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Dubbo {@link ClientHttpResponse} implementation.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DubboTransporterInterceptor
 */
class DubboClientHttpResponse implements ClientHttpResponse {

	private final HttpStatus httpStatus;

	private final String statusText;

	private final HttpHeaders httpHeaders = new HttpHeaders();

	private final DubboHttpOutputMessage httpOutputMessage;

	DubboClientHttpResponse(DubboHttpOutputMessage httpOutputMessage,
			GenericException exception) {
		this.httpStatus = exception != null ? HttpStatus.INTERNAL_SERVER_ERROR
				: HttpStatus.OK;
		this.statusText = exception != null ? exception.getExceptionMessage()
				: httpStatus.getReasonPhrase();
		this.httpOutputMessage = httpOutputMessage;
		this.httpHeaders.putAll(httpOutputMessage.getHeaders());
	}

	@Override
	public HttpStatus getStatusCode() throws IOException {
		return httpStatus;
	}

	@Override
	public int getRawStatusCode() throws IOException {
		return httpStatus.value();
	}

	@Override
	public String getStatusText() throws IOException {
		return statusText;
	}

	@Override
	public void close() {
	}

	@Override
	public InputStream getBody() throws IOException {
		return httpOutputMessage.getBody().getInputStream();
	}

	@Override
	public HttpHeaders getHeaders() {
		return httpHeaders;
	}

}
