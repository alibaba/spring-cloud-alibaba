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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;

import feign.Client;
import feign.Request;
import feign.Response;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

/**
 * @author xiaojing
 */
public class FescarLoadBalancerFeignClient extends LoadBalancerFeignClient {

	private final BeanFactory beanFactory;

	FescarLoadBalancerFeignClient(Client delegate,
			CachingSpringLoadBalancerFactory lbClientFactory,
			SpringClientFactory clientFactory, BeanFactory beanFactory) {
		super(wrap(delegate, beanFactory), lbClientFactory, clientFactory);
		this.beanFactory = beanFactory;
	}

	@Override
	public Response execute(Request request, Request.Options options) throws IOException {
		return super.execute(request, options);
	}

	private static Client wrap(Client delegate, BeanFactory beanFactory) {
		return (Client) new FescarFeignObjectWrapper(beanFactory).wrap(delegate);
	}

}
