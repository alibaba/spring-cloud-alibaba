package com.alibaba.cloud.governance.auth.rules.util;

import com.alibaba.cloud.governance.auth.rules.auth.IpBlockRule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public class IpUtil {

	private static final String UNKNOWN = "unknown";

	public static String getRemoteIpAddress(ServerHttpRequest request) {
		String ip = request.getHeaders().getFirst("X-Forwarded-For");
		if (StringUtils.isNotEmpty(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
			if (ip.contains(",")) {
				ip = ip.split(",")[0];
			}
		}
		if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
			if (request.getRemoteAddress() != null
					&& request.getRemoteAddress().getAddress() != null) {
				ip = request.getRemoteAddress().getAddress().getHostAddress();
			}
		}
		return StringUtils.isEmpty(ip) ? null : ip;
	}

	public static String getRemoteIpAddress(ServletRequest request) {
		if (!(request instanceof HttpServletRequest)) {
			return null;
		}
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String ip = httpServletRequest.getHeader("X-Forwarded-For");
		if (StringUtils.isNotEmpty(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
			if (ip.contains(",")) {
				ip = ip.split(",")[0];
			}
		}
		if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
			if (httpServletRequest.getRemoteAddr() != null) {
				ip = httpServletRequest.getRemoteAddr();
			}
		}
		return StringUtils.isEmpty(ip) ? null : ip;
	}

}
