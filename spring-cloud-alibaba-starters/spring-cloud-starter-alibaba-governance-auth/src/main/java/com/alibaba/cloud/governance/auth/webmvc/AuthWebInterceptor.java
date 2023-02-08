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

package com.alibaba.cloud.governance.auth.webmvc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.cloud.governance.auth.util.IpUtil;
import com.alibaba.cloud.governance.auth.validator.AuthValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class AuthWebInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(AuthWebInterceptor.class);

	private final AuthValidator authValidator;

	public AuthWebInterceptor(AuthValidator authValidator) {
		this.authValidator = authValidator;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
		String sourceIp = request.getRemoteAddr();
		String destIp = request.getLocalAddr();
		String remoteIp = IpUtil.getRemoteIpAddress(request);
		String host = request.getHeader(HttpHeaders.HOST);
		String method = request.getMethod();
		String path = request.getRequestURI();
		int port = request.getLocalPort();
		HttpHeaders headers = getHeaders(request);
		MultiValueMap<String, String> params = getQueryParams(request);
		AuthValidator.UnifiedHttpRequest.UnifiedHttpRequestBuilder builder = new AuthValidator.UnifiedHttpRequest.UnifiedHttpRequestBuilder();
		AuthValidator.UnifiedHttpRequest unifiedHttpRequest = builder.setDestIp(destIp)
				.setRemoteIp(remoteIp).setSourceIp(sourceIp).setHost(host).setPort(port)
				.setMethod(method).setPath(path).setHeaders(headers).setParams(params)
				.build();
		if (!authValidator.validate(unifiedHttpRequest)) {
			return ret401(response);
		}
		return true;
	}

	private HttpHeaders getHeaders(HttpServletRequest request) {
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			try {
				String key = headerNames.nextElement();
				key = key.substring(0, 1).toUpperCase() + key.substring(1);
				Enumeration<String> headerValues = request.getHeaders(key);
				while (headerValues.hasMoreElements()) {
					headers.add(key, headerValues.nextElement());
				}
			}
			catch (Exception e) {
				log.error("Unknown header key", e);
			}
		}
		return headers;
	}

	private MultiValueMap<String, String> getQueryParams(HttpServletRequest request) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String key = paramNames.nextElement();
			String[] paramValues = request.getParameterValues(key);
			params.addAll(key, Arrays.asList(paramValues));
		}
		return params;
	}

	private boolean ret401(HttpServletResponse response) throws IOException {
		return ret401(response, "Auth failed, please check the request and auth rule");
	}

	private boolean ret401(HttpServletResponse response, String errorMsg)
			throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().println(errorMsg);
		return false;
	}

}
