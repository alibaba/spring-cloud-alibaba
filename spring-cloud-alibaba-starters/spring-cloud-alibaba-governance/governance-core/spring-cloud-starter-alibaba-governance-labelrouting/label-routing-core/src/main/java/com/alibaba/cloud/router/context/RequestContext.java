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

package com.alibaba.cloud.router.context;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author HH
 */
public class RequestContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestContext.class);

	private static final ThreadLocal<HttpServletRequest> requestHeadersHolder = new ThreadLocal<>();

	public HttpServletRequest getRequest(Boolean ifRemove) {
		if (ifRemove) {
			return getRequest();
		}

		return requestHeadersHolder.get();
	}

	private HttpServletRequest getRequest() {
		HttpServletRequest request;

		try {
			request = requestHeadersHolder.get();
		}
		finally {
			requestHeadersHolder.remove();
		}
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		requestHeadersHolder.set(request);
	}

}
