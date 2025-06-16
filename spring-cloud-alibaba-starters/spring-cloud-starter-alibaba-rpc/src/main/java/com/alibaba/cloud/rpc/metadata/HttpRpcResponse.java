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

package com.alibaba.cloud.rpc.metadata;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * @author :Lictory
 * @date : 2024/08/16
 */
public class HttpRpcResponse implements Serializable {
	private int statusCode;
	private String reasonPhrase;
	private Map<String, Collection<String>> headers;
	private byte[] body;

	public HttpRpcResponse(int statusCode, String reasonPhrase, Map<String, Collection<String>> headers, byte[] body) {
		this.statusCode = statusCode;
		this.reasonPhrase = reasonPhrase;
		this.headers = headers;
		this.body = body;
	}

	public HttpRpcResponse() {
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	public Map<String, Collection<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Collection<String>> headers) {
		this.headers = headers;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
}

