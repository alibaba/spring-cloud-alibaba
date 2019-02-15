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

package org.springframework.cloud.alibaba.fescar.feign;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fescar.core.context.RootContext;

import org.springframework.beans.factory.BeanFactory;

import feign.Client;
import feign.Request;
import feign.Response;
import org.springframework.util.StringUtils;

/**
 * @author xiaojing
 */
public class FescarFeignClient implements Client {

	private final Client delegate;
	private final BeanFactory beanFactory;

	FescarFeignClient(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.delegate = new Client.Default(null, null);
	}

	FescarFeignClient(BeanFactory beanFactory, Client delegate) {
		this.delegate = delegate;
		this.beanFactory = beanFactory;
	}

	@Override
	public Response execute(Request request, Request.Options options) throws IOException {

		Request modifiedRequest = getModifyRequest(request);

		try {
			return this.delegate.execute(modifiedRequest, options);
		}
		finally {

		}
	}

	private Request getModifyRequest(Request request) {

		String xid = RootContext.getXID();

		if (StringUtils.isEmpty(xid)) {
			return request;
		}

		Map<String, Collection<String>> headers = new HashMap<>();
		headers.putAll(request.headers());

		List<String> fescarXid = new ArrayList<>();
		fescarXid.add(xid);
		headers.put(RootContext.KEY_XID, fescarXid);

		return Request.create(request.method(), request.url(), headers, request.body(),
				request.charset());
	}

}