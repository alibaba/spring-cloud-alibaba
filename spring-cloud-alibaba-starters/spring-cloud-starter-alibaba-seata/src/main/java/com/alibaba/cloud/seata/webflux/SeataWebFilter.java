/*
 * Copyright 2013-present the original author or authors.
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

package com.alibaba.cloud.seata.webflux;

import org.apache.seata.common.util.StringUtils;
import org.apache.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

/**
 * @author ChangJin Wei (魏昌进)
 */
public class SeataWebFilter implements WebFilter {

	private static final Logger log = LoggerFactory.getLogger(SeataWebFilter.class);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String xid = RootContext.getXID();
		String rpcXid = exchange.getRequest().getHeaders().getFirst(RootContext.KEY_XID);
		if (log.isDebugEnabled()) {
			log.debug("xid in RootContext {} xid in RpcContext {}", xid, rpcXid);
		}

		if (StringUtils.isBlank(xid) && rpcXid != null) {
			RootContext.bind(rpcXid);
			if (log.isDebugEnabled()) {
				log.debug("bind {} to RootContext", rpcXid);
			}
		}

		return chain.filter(exchange)
				.doFinally(sig -> {
					if (StringUtils.isNotBlank(RootContext.getXID())) {
						String headerXid = exchange.getRequest().getHeaders().getFirst(RootContext.KEY_XID);

						if (StringUtils.isEmpty(headerXid)) {
							return;
						}

						String unbindXid = RootContext.unbind();
						if (log.isDebugEnabled()) {
							log.debug("unbind {} from RootContext", unbindXid);
						}
						if (!headerXid.equalsIgnoreCase(unbindXid)) {
							log.warn("xid in change during RPC from {} to {}", headerXid, unbindXid);
							if (unbindXid != null) {
								RootContext.bind(unbindXid);
								log.warn("bind {} back to RootContext", unbindXid);
							}
						}
					}
				});
	}
}
