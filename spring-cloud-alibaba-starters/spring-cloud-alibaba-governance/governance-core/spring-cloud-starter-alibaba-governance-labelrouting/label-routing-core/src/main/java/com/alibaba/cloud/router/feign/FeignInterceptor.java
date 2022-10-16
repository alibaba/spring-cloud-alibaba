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

import com.alibaba.cloud.router.util.RequestContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author HH
 */
public class FeignInterceptor implements RequestInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(FeignInterceptor.class);

	@Autowired
	private RequestContext requestContext;

	@Override
	public void apply(RequestTemplate requestTemplate) {
		final HttpServletRequest request = requestContext.getRequest(false);
		final Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames == null) {
			return;
		}
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			requestTemplate.header(headerName, request.getHeader(headerName));
		}
	}

}
