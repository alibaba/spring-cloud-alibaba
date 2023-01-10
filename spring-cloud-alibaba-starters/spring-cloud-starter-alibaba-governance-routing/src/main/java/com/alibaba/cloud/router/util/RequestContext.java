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

package com.alibaba.cloud.router.util;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author HH
 */
public final class RequestContext {

	private static final Logger LOG = LoggerFactory.getLogger(RequestContext.class);

	private static final ThreadLocal<HttpServletRequest> REQUEST_THREAD_HOLDER = new ThreadLocal<>();

	private RequestContext() {
	}

	public static HttpServletRequest getRequest() {
		return REQUEST_THREAD_HOLDER.get();
	}

	public static void setRequest(HttpServletRequest request) {
		REQUEST_THREAD_HOLDER.set(request);
	}

	public static void removeRequest() {
		REQUEST_THREAD_HOLDER.remove();
	}

}
