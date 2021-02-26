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

package com.alibaba.cloud.seata.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.seata.common.util.StringUtils;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author xiaojing
 *
 * Seata HandlerInterceptor, Convert Seata information into
 * @see io.seata.core.context.RootContext from http request's header in
 * {@link org.springframework.web.servlet.HandlerInterceptor#preHandle(HttpServletRequest , HttpServletResponse , Object )},
 * And clean up Seata information after servlet method invocation in
 * {@link org.springframework.web.servlet.HandlerInterceptor#afterCompletion(HttpServletRequest, HttpServletResponse, Object, Exception)}
 */
public class SeataHandlerInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory
			.getLogger(SeataHandlerInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) {
		String xid = RootContext.getXID();
		String rpcXid = request.getHeader(RootContext.KEY_XID);
		if (log.isDebugEnabled()) {
			log.debug("xid in RootContext {} xid in RpcContext {}", xid, rpcXid);
		}

		if (StringUtils.isBlank(xid) && rpcXid != null) {
			RootContext.bind(rpcXid);
			if (log.isDebugEnabled()) {
				log.debug("bind {} to RootContext", rpcXid);
			}
		}

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception e) {
		if (StringUtils.isNotBlank(RootContext.getXID())) {
			String rpcXid = request.getHeader(RootContext.KEY_XID);

			if (StringUtils.isEmpty(rpcXid)) {
				return;
			}

			String unbindXid = RootContext.unbind();
			if (log.isDebugEnabled()) {
				log.debug("unbind {} from RootContext", unbindXid);
			}
			if (!rpcXid.equalsIgnoreCase(unbindXid)) {
				log.warn("xid in change during RPC from {} to {}", rpcXid, unbindXid);
				if (unbindXid != null) {
					RootContext.bind(unbindXid);
					log.warn("bind {} back to RootContext", unbindXid);
				}
			}
		}
	}

}
