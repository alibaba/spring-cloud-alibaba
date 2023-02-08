/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.feign;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author HH
 * @since 2.2.10-RC1
 */
public class RoutingFeignInterceptor implements RequestInterceptor {

	private static final Logger LOG = LoggerFactory
			.getLogger(RoutingFeignInterceptor.class);

	@Override
	public void apply(RequestTemplate requestTemplate) {
		final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
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
