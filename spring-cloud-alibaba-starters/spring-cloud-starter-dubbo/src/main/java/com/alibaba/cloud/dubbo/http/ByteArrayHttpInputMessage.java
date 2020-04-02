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

import org.apache.dubbo.common.io.UnsafeByteArrayInputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;

/**
 * Byte array {@link HttpInputMessage} implementation.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
class ByteArrayHttpInputMessage implements HttpInputMessage {

	private final HttpHeaders httpHeaders;

	private final InputStream inputStream;

	ByteArrayHttpInputMessage(byte[] body) {
		this(new HttpHeaders(), body);
	}

	ByteArrayHttpInputMessage(HttpHeaders httpHeaders, byte[] body) {
		this.httpHeaders = httpHeaders;
		this.inputStream = new UnsafeByteArrayInputStream(body);
	}

	@Override
	public InputStream getBody() throws IOException {
		return inputStream;
	}

	@Override
	public HttpHeaders getHeaders() {
		return httpHeaders;
	}

}
