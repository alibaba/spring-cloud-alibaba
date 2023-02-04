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

package com.alibaba.cloud.examples.kubernetes.config.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.cloud.examples.kubernetes.config.properties.BlacklistProperties;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Freeman
 */
@Component
public class BlacklistFilter extends OncePerRequestFilter {

	private final BlacklistProperties blacklistProperties;

	public BlacklistFilter(BlacklistProperties blacklistProperties) {
		this.blacklistProperties = blacklistProperties;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp,
			FilterChain chain) throws ServletException, IOException {
		String userIdHeader = blacklistProperties.getHeader();
		String userId = req.getHeader(userIdHeader);
		if (userId == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					userIdHeader + " header is required!");
			return;
		}
		if (blacklistProperties.getUserIds().contains(userId)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN,
					String.format("User %s has been blocked!", userId));
			return;
		}
		chain.doFilter(req, resp);
	}
}
