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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.util.FastByteArrayOutputStream;

/**
 * Dubbo {@link HttpOutputMessage} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
class DubboHttpOutputMessage implements HttpOutputMessage {

	private final FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();

	private final HttpHeaders httpHeaders = new HttpHeaders();

	@Override
	public FastByteArrayOutputStream getBody() throws IOException {
		return outputStream;
	}

	@Override
	public HttpHeaders getHeaders() {
		return httpHeaders;
	}
}
