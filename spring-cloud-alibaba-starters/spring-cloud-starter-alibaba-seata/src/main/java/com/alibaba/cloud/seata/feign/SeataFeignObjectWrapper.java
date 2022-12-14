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

package com.alibaba.cloud.seata.feign;

import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;

/**
 * @author xiaojing
 */
public class SeataFeignObjectWrapper {

	private static final Logger LOG = LoggerFactory
			.getLogger(SeataFeignObjectWrapper.class);

	private final BeanFactory beanFactory;


	SeataFeignObjectWrapper(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	Object wrap(Object bean) {
		if (bean instanceof Client client && !(bean instanceof SeataFeignClient)) {
			if (bean instanceof FeignBlockingLoadBalancerClient feignBlockingLoadBalancerClient) {
				return new SeataFeignBlockingLoadBalancerClient(feignBlockingLoadBalancerClient.getDelegate(),
						beanFactory.getBean(BlockingLoadBalancerClient.class),
						beanFactory.getBean(LoadBalancerClientFactory.class),
						this);
			}
			return new SeataFeignClient(this.beanFactory, client);
		}
		return bean;
	}
}
