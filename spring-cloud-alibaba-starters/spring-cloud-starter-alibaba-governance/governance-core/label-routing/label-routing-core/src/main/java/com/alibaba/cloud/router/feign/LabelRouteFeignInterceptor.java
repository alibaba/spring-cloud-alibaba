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

package com.alibaba.cloud.router.feign;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.router.cache.RequestCache;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author HH
 */
public class LabelRouteFeignInterceptor implements RequestInterceptor {

	@Autowired
	private RequestCache requestCache;

	@Override
	public void apply(RequestTemplate requestTemplate) {
		HttpServletRequest request = requestCache.getRequest();
		Enumeration<String> headerNames = request.getHeaderNames();

		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			requestTemplate.header(headerName, request.getHeader(headerName));
		}
	}

}
