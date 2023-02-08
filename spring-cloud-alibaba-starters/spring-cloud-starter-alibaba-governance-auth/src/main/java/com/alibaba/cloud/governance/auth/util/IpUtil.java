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

package com.alibaba.cloud.governance.auth.util;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.commons.lang.StringUtils;

import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public final class IpUtil {

	private static final String UNKNOWN = "unknown";

	private IpUtil() {

	}

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
