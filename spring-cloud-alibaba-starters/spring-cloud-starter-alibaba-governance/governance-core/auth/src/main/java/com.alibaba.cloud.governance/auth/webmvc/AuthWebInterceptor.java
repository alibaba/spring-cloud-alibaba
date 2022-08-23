package com.alibaba.cloud.governance.auth.webmvc;

import com.alibaba.cloud.governance.common.rules.manager.*;
import com.alibaba.cloud.governance.common.rules.util.IpUtil;
import org.jose4j.jwt.JwtClaims;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Enumeration;

public class AuthWebInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
		String sourceIp = request.getRemoteAddr(), destIp = request.getLocalAddr(),
				remoteIp = IpUtil.getRemoteIpAddress(request);
		if (!IpBlockRuleManager.isValid(sourceIp, destIp, remoteIp)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
		String host = request.getRemoteHost();
		String method = request.getMethod();
		String path = request.getRequestURI();
		int port = request.getLocalPort();
		if (!TargetRuleManager.isValid(host, port, method, path)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
		HttpHeaders headers = getHeaders(request);
		if (!HeaderRuleManager.isValid(headers)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
		JwtClaims jwtClaims = null;
		MultiValueMap<String, String> params = getQueryParams(request);
		if ((jwtClaims = JwtRuleManager.isValid(params, headers)) == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
		if (!JwtAuthRuleManager.isValid(jwtClaims)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
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

}
