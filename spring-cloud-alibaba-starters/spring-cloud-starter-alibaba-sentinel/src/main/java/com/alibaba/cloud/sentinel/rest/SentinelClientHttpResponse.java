/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.sentinel.rest;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;


public class SentinelClientHttpResponse extends BaseHttpInputMessage implements ClientHttpResponse {

	private final HttpStatusCode statusCode;

	private static final String BLOCK_RESPONSE = "RestTemplate request block by sentinel";
	//private String blockResponse = BLOCK_RESPONSE;

	/**
	 * Create a {@code SentinelMyClientHttpResponse} with an empty response body and
	 * HTTP status code {@link HttpStatus#OK OK}.
	 *
	 * @since 6.0.3
	 */
	public SentinelClientHttpResponse() {
		this(BLOCK_RESPONSE.getBytes(), HttpStatus.OK);
	}

	public SentinelClientHttpResponse(String blockResponse) {
		this(blockResponse.getBytes(), HttpStatus.OK);
	}

	/**
	 * Create a {@code SentinelMyClientHttpResponse} with response body as a byte array
	 * and the supplied HTTP status code.
	 */
	public SentinelClientHttpResponse(byte[] body, HttpStatusCode statusCode) {
		super(body);
		Assert.notNull(statusCode, "HttpStatusCode must not be null");
		this.statusCode = statusCode;
	}

	/**
	 * Create a {@code SentinelMyClientHttpResponse} with response body as a byte array
	 * and a custom HTTP status code.
	 *
	 * @since 5.3.17
	 */
	public SentinelClientHttpResponse(byte[] body, int statusCode) {
		this(body, HttpStatusCode.valueOf(statusCode));
	}

	/**
	 * Create a {@code SentinelMyClientHttpResponse} with response body as {@link InputStream}
	 * and the supplied HTTP status code.
	 */
	public SentinelClientHttpResponse(InputStream body, HttpStatusCode statusCode) {
		super(body);
		Assert.notNull(statusCode, "HttpStatusCode must not be null");
		this.statusCode = statusCode;
	}

	/**
	 * Create a {@code SentinelMyClientHttpResponse} with response body as {@link InputStream}
	 * and a custom HTTP status code.
	 *
	 * @since 5.3.17
	 */
	public SentinelClientHttpResponse(InputStream body, int statusCode) {
		this(body, HttpStatusCode.valueOf(statusCode));
	}


	@Override
	public HttpStatusCode getStatusCode() {
		return this.statusCode;
	}

	@Override
	public String getStatusText() {
		return (this.statusCode instanceof HttpStatus status ? status.getReasonPhrase() : "");
	}

	@Override
	public void close() {
		try {
			getBody().close();
		}
		catch (IOException ex) {
			// ignore
		}
	}

}
