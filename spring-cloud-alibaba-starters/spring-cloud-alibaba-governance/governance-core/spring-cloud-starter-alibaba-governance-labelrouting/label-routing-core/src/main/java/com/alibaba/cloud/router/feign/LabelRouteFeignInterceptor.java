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
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.router.context.RequestContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author HH
 */
public class LabelRouteFeignInterceptor implements RequestInterceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelRouteFeignInterceptor.class);

	@Autowired
	private RequestContext requestContext;

	@Override
	public void apply(RequestTemplate requestTemplate) {
		final HttpServletRequest request = requestContext.getRequest(false);
		final Optional<Enumeration<String>> headerNames = Optional.ofNullable(request.getHeaderNames());

		if (!headerNames.isPresent()) {
			return;
		}

		while (headerNames.get().hasMoreElements()) {
			String headerName = headerNames.get().nextElement();
			requestTemplate.header(headerName, request.getHeader(headerName));
		}
	}
}
