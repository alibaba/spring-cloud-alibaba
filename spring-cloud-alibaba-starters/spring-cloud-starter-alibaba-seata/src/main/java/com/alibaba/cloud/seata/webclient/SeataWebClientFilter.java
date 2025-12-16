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

package com.alibaba.cloud.seata.webclient;

import org.apache.seata.core.context.RootContext;
import reactor.core.publisher.Mono;

import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

/**
 * @author ChangJin Wei (魏昌进)
 */
public class SeataWebClientFilter implements ExchangeFilterFunction {

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		String xid = RootContext.getXID();
		if (!StringUtils.hasLength(xid)) {
			return next.exchange(request);
		}
		ClientRequest newReq = ClientRequest.from(request)
				.headers(h -> h.add(RootContext.KEY_XID, xid))
				.build();
		return next.exchange(newReq);
	}


}
