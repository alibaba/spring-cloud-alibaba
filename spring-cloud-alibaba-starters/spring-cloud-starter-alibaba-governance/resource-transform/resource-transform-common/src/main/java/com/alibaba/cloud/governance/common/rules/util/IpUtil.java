package com.alibaba.cloud.governance.common.rules.util;

import com.alibaba.cloud.governance.common.rules.auth.IpBlockRule;
import com.google.common.net.HttpHeaders;
import io.envoyproxy.envoy.config.core.v3.CidrRange;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public class IpUtil {

	private static final Logger log = LoggerFactory.getLogger(IpBlockRule.class);

	private static final String UNKNOWN = "unknown";

	public static boolean matchIp(String remoteIp, CidrRange rule) {
		String remoteIpBinary = ip2BinaryString(remoteIp);
		if (StringUtils.isEmpty(remoteIpBinary)) {
			return false;
		}
		String prefix = rule.getAddressPrefix();
		String prefixBinary = ip2BinaryString(prefix);
		if (StringUtils.isEmpty(prefixBinary)) {
			return false;
		}
		if (!rule.hasPrefixLen()) {
			return remoteIpBinary.equals(prefixBinary);
		}
		int prefixLen = rule.getPrefixLen().getValue();
		if (remoteIpBinary.length() >= prefixLen && prefixBinary.length() >= prefixLen) {
			return remoteIpBinary.substring(0, prefixLen)
					.equals(prefixBinary.substring(0, prefixLen));
		}
		return false;
	}

	/**
	 * @param ip dotted ip string, for example: 127.0.0.1
	 * @return
	 */
	public static String ip2BinaryString(String ip) {
		try {
			String[] ips = ip.split("\\.");
			long[] ipLong = new long[4];
			for (int i = 0; i < 4; ++i) {
				ipLong[i] = Long.parseLong(ips[i]);
				if (ipLong[i] < 0 || ipLong[i] > 255) {
					return "";
				}
			}
			return String
					.format("%32s", Long.toBinaryString((ipLong[0] << 24)
							+ (ipLong[1] << 16) + (ipLong[2] << 8) + ipLong[3]))
					.replace(" ", "0");
		}
		catch (Exception e) {
			log.error("failed to parse ip {} to binary string", ip);
		}
		return "";
	}

	public static String getRemoteIpAddress(ServerHttpRequest request) {
		String ip = request.getHeaders().getFirst(HttpHeaders.X_FORWARDED_FOR);
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
		String ip = httpServletRequest.getHeader(HttpHeaders.X_FORWARDED_FOR);
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
