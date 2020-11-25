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

	private final HttpStatus HTTP_STATUS;

	private final String STATUS_TEXT;

	private final HttpHeaders HTTP_HEADERS = new HttpHeaders();

	private final DubboHttpOutputMessage HTTP_OUTPUT_MESSAGE;

	DubboClientHttpResponse(DubboHttpOutputMessage httpOutputMessage,
			GenericException exception) {
		this.HTTP_STATUS = exception != null ? HttpStatus.INTERNAL_SERVER_ERROR
				: HttpStatus.OK;
		this.STATUS_TEXT = exception != null ? exception.getExceptionMessage()
				: HTTP_STATUS.getReasonPhrase();
		this.HTTP_OUTPUT_MESSAGE = httpOutputMessage;
		this.HTTP_HEADERS.putAll(httpOutputMessage.getHeaders());
	}

	@Override
	public HttpStatus getStatusCode() throws IOException {
		return HTTP_STATUS;
	}

	@Override
	public int getRawStatusCode() throws IOException {
		return HTTP_STATUS.value();
	}

	@Override
	public String getStatusText() throws IOException {
		return STATUS_TEXT;
	}

	@Override
	public void close() {
	}

	@Override
	public InputStream getBody() throws IOException {
		return HTTP_OUTPUT_MESSAGE.getBody().getInputStream();
	}

	@Override
	public HttpHeaders getHeaders() {
		return HTTP_HEADERS;
	}

}
