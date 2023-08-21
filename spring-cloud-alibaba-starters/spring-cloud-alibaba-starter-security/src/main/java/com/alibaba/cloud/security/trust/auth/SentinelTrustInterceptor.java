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

package com.alibaba.cloud.security.trust.auth;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.trust.TrustManager;
import com.alibaba.csp.sentinel.trust.tls.TlsMode;
import com.alibaba.csp.sentinel.trust.validator.AuthValidator;
import com.alibaba.csp.sentinel.trust.validator.UnifiedHttpRequest;
import com.alibaba.csp.sentinel.util.StringUtil;

import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;







public class SentinelTrustInterceptor implements HandlerInterceptor {

	private static final String UNKNOWN_IP = "unknown";
	private TrustManager trustManager = TrustManager.getInstance();

	public static String getPrincipal(X509Certificate x509Certificate) {
		try {
			Collection<List<?>> san = x509Certificate.getSubjectAlternativeNames();
			return (String) san.iterator().next().get(1);
		}
		catch (Exception e) {
			RecordLog.error("Failed to get istio SAN from X509Certificate", e);
		}
		return null;
	}

	public static String getRemoteIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		//Only select the first one
		if (StringUtil.isNotEmpty(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
			if (ip.contains(",")) {
				ip = ip.split(",")[0];
				return ip;
			}
		}
		if (null != request.getRemoteAddr()) {
			return request.getRemoteAddr();
		}
		return null;
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (null == trustManager.getTlsMode()) {
			return true;
		}
		//Don't authenticate in DISABLE mode
		int port = request.getLocalPort();
		TlsMode tlsMode = trustManager.getTlsMode();
		TlsMode.TlsType currentTlsType = tlsMode.getPortTls(port);
		if (TlsMode.TlsType.DISABLE == currentTlsType) {
			return true;
		}

		X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
		boolean notHaveCert = (null == certs || 0 == certs.length);
		//When no cert,don't authenticate in PERMISSIVE mode
		if (notHaveCert) {
			if (TlsMode.TlsType.STRICT == currentTlsType) {
				return false;
			}

			if (TlsMode.TlsType.PERMISSIVE == currentTlsType) {
				return true;
			}
		}

		if (null == trustManager.getRules()) {
			return true;
		}

		String principal = getPrincipal(certs[0]);
		String sourceIp = request.getRemoteAddr();
		String destIp = request.getLocalAddr();
		String remoteIp = getRemoteIpAddress(request);
		String host = request.getHeader(HttpHeaders.HOST);
		String method = request.getMethod();
		String path = request.getRequestURI();
		Map<String, List<String>> headers = getHeaders(request);
		Map<String, List<String>> params = getParams(request);
		String sni = request.getServerName();

		UnifiedHttpRequest.UnifiedHttpRequestBuilder builder = new UnifiedHttpRequest.UnifiedHttpRequestBuilder();
		UnifiedHttpRequest unifiedHttpRequest = builder
				.setDestIp(destIp)
				.setRemoteIp(remoteIp)
				.setSourceIp(sourceIp)
				.setHost(host)
				.setPort(port)
				.setMethod(method)
				.setPath(path)
				.setHeaders(headers)
				.setParams(params)
				.setPrincipal(principal)
				.setSni(sni)
				.build();

		return AuthValidator.validate(unifiedHttpRequest, trustManager.getRules());
	}

	private Map<String, List<String>> getHeaders(HttpServletRequest request) {
		Map<String, List<String>> headers = new HashMap<>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			Enumeration<String> headerValues = request.getHeaders(key);
			List<String> values = new ArrayList<>();
			while (headerValues.hasMoreElements()) {
				values.add(headerValues.nextElement());
			}
			headers.put(key, values);

		}
		return headers;
	}

	private Map<String, List<String>> getParams(HttpServletRequest request) {
		Map<String, List<String>> params = new HashMap<>();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String key = paramNames.nextElement();
			List<String> values = Arrays.asList(request.getParameterValues(key));
			params.put(key, values);
		}
		return params;
	}


}
