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
public class HttpMetadata implements Serializable {

	private String url;

	private String method;

	private byte[] body;

	private Map<String, Collection<String>> headers;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public Map<String, Collection<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Collection<String>> headers) {
		this.headers = headers;
	}
}
