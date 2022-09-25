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

package com.alibaba.cloud.governance.auth.webmvc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.cloud.governance.auth.AuthValidator;
import com.alibaba.cloud.governance.auth.util.IpUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jwt.JwtClaims;

import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthWebInterceptor implements HandlerInterceptor {

	private AuthValidator authValidator;

	public AuthWebInterceptor(AuthValidator authValidator) {
		this.authValidator = authValidator;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
		String sourceIp = request.getRemoteAddr();
		String destIp = request.getLocalAddr();
		String remoteIp = IpUtil.getRemoteIpAddress(request);
		if (!authValidator.validateIp(sourceIp, destIp, remoteIp)) {
			return ret401(response);
		}
		String host = request.getHeader(HttpHeaders.HOST);
		String method = request.getMethod();
		String path = request.getRequestURI();
		int port = request.getLocalPort();
		if (!authValidator.validateTargetRule(host, port, method, path)) {
			return ret401(response);
		}
		HttpHeaders headers = getHeaders(request);
		if (!authValidator.validateHeader(headers)) {
			return ret401(response);
		}
		JwtClaims jwtClaims = null;
		MultiValueMap<String, String> params = getQueryParams(request);
		if (!authValidator.isEmptyJwtRule()) {
			Pair<JwtClaims, Boolean> jwtClaimsBooleanPair = authValidator
					.validateJwt(params, headers);
			if (!jwtClaimsBooleanPair.getRight()) {
				return ret401(response);
			}
			jwtClaims = jwtClaimsBooleanPair.getLeft();
		}

		if (jwtClaims == null && authValidator.isEmptyJwtAuthRule()) {
			return true;
		}

		if (!authValidator.validateJwtAuthRule(jwtClaims)) {
			return ret401(response);
		}
		return true;
	}

	private HttpHeaders getHeaders(HttpServletRequest request) {
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			Enumeration<String> headerValues = request.getHeaders(key);
			while (headerValues.hasMoreElements()) {
				headers.add(key, headerValues.nextElement());
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
