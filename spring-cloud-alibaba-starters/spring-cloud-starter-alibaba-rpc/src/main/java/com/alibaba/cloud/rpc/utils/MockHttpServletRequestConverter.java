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

package com.alibaba.cloud.rpc.utils;

import java.util.Collection;
import java.util.Map;

import com.alibaba.cloud.rpc.metadata.HttpMetadata;

import org.springframework.mock.web.MockHttpServletRequest;


/**
 * @author :Lictory
 * @date : 2024/08/23
 */
public final class MockHttpServletRequestConverter {
	private MockHttpServletRequestConverter() {

	}

	public static MockHttpServletRequest getMockHttpServletRequest(HttpMetadata httpMetadata, String url) {
		// create MockHttpServletRequest instance
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();

		// set HTTP method
		mockRequest.setMethod(httpMetadata.getMethod());

		// set URL
		mockRequest.setRequestURI(url);

		// set request headers
		Map<String, Collection<String>> headers = httpMetadata.getHeaders();
		for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
			String headerName = entry.getKey();
			for (String headerValue : entry.getValue()) {
				mockRequest.addHeader(headerName, headerValue);
			}
		}
		// set request body（if exist）
		if (httpMetadata.getBody() != null) {
			mockRequest.setContent(httpMetadata.getBody());
		}
		return mockRequest;
	}
}
