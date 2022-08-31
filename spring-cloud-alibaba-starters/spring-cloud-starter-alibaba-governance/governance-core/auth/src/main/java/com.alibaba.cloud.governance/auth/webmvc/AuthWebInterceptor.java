package com.alibaba.cloud.governance.auth.webmvc;

import com.alibaba.cloud.governance.auth.rules.manager.*;
import com.alibaba.cloud.governance.auth.rules.util.IpUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jwt.JwtClaims;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

public class AuthWebInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
		String sourceIp = request.getRemoteAddr(), destIp = request.getLocalAddr(),
				remoteIp = IpUtil.getRemoteIpAddress(request);
		if (!IpBlockRuleManager.isValid(sourceIp, destIp, remoteIp)) {
			return ret401(response);
		}
		String host = request.getHeader(HttpHeaders.HOST);
		String method = request.getMethod();
		String path = request.getRequestURI();
		int port = request.getLocalPort();
		if (!TargetRuleManager.isValid(host, port, method, path)) {
			return ret401(response);
		}
		HttpHeaders headers = getHeaders(request);
		if (!HeaderRuleManager.isValid(headers)) {
			return ret401(response);
		}
		JwtClaims jwtClaims = null;
		MultiValueMap<String, String> params = getQueryParams(request);
		if (!JwtRuleManager.isEmpty()) {
			Pair<JwtClaims, Boolean> jwtClaimsBooleanPair = JwtRuleManager.isValid(params,
					headers);
			if (!jwtClaimsBooleanPair.getRight()) {
				return ret401(response);
			}
			jwtClaims = jwtClaimsBooleanPair.getLeft();
		}

		if (jwtClaims == null && JwtAuthRuleManager.isEmpty()) {
			return true;
		}

		if (!JwtAuthRuleManager.isValid(jwtClaims)) {
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
