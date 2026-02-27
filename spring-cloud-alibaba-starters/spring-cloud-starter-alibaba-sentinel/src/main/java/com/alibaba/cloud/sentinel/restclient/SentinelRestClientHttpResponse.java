/*
 * Copyright 2013-present the original author or authors.
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

package com.alibaba.cloud.sentinel.restclient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Default {@link ClientHttpResponse} returned when a request is blocked by
 * Sentinel
 * (flow control or circuit breaking) in RestClient integration.
 *
 * @author uuuyuqi
 */
public class SentinelRestClientHttpResponse implements ClientHttpResponse {

	private static final String DEFAULT_BLOCK_RESPONSE = "RestClient request blocked by Sentinel";

	private final String blockResponse;

	public SentinelRestClientHttpResponse() {
		this(DEFAULT_BLOCK_RESPONSE);
	}

	public SentinelRestClientHttpResponse(String blockResponse) {
		this.blockResponse = blockResponse;
	}

	@Override
	public HttpStatusCode getStatusCode() {
		return HttpStatus.TOO_MANY_REQUESTS;
	}

	@Override
	public String getStatusText() {
		return blockResponse;
	}

	@Override
	public void close() {
		// nothing to do
	}

	@Override
	public InputStream getBody() {
		return new ByteArrayInputStream(blockResponse.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public HttpHeaders getHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

}
