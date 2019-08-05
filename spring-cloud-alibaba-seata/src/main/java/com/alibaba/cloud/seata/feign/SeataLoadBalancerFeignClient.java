/*
 * Copyright (C) 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.seata.feign;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.util.StringUtils;

import feign.Client;
import feign.Request;
import feign.Response;
import io.seata.core.context.RootContext;

/**
 * @author xiaojing
 */
public class SeataLoadBalancerFeignClient extends LoadBalancerFeignClient {

	private static final int MAP_SIZE = 16;

	private final BeanFactory beanFactory;

	SeataLoadBalancerFeignClient(Client delegate,
			CachingSpringLoadBalancerFactory lbClientFactory,
			SpringClientFactory clientFactory, BeanFactory beanFactory) {
		super(wrap(delegate, beanFactory), lbClientFactory, clientFactory);
		this.beanFactory = beanFactory;
	}

	@Override
	public Response execute(Request request, Request.Options options) throws IOException {
		Request modifiedRequest = getModifyRequest(request);
		return super.execute(modifiedRequest, options);
	}

	private static Client wrap(Client delegate, BeanFactory beanFactory) {
		return (Client) new SeataFeignObjectWrapper(beanFactory).wrap(delegate);
	}

	private Request getModifyRequest(Request request) {

		String xid = RootContext.getXID();

		if (StringUtils.isEmpty(xid)) {
			return request;
		}

		Map<String, Collection<String>> headers = new HashMap<>(MAP_SIZE);
		headers.putAll(request.headers());

		List<String> seataXid = new ArrayList<>();
		seataXid.add(xid);
		headers.put(RootContext.KEY_XID, seataXid);

		return Request.create(request.method(), request.url(), headers, request.body(),
				request.charset());
	}
}
