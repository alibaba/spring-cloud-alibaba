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

import java.lang.reflect.Field;

import feign.Client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

/**
 * @author xiaojing
 */
public class SeataFeignObjectWrapper {

	private static final Log LOG = LogFactory.getLog(SeataFeignObjectWrapper.class);

	private final BeanFactory beanFactory;

	private CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory;

	private SpringClientFactory springClientFactory;

	private BlockingLoadBalancerClient loadBalancerClient;

	SeataFeignObjectWrapper(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	Object wrap(Object bean) {
		if (bean instanceof Client && !(bean instanceof SeataFeignClient)) {
			if (bean instanceof LoadBalancerFeignClient) {
				LoadBalancerFeignClient client = ((LoadBalancerFeignClient) bean);
				return new SeataLoadBalancerFeignClient(client.getDelegate(), factory(),
						clientFactory(), this.beanFactory);
			}
			if (bean.getClass().getName().equals(
					"org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient")) {
				return new SeataFeignBlockingLoadBalancerClient(getClient(bean),
						loadBalancerClient());
			}
			return new SeataFeignClient(this.beanFactory, (Client) bean);
		}
		return bean;
	}

	private Client getClient(Object bean) {
		Field client = null;
		boolean oldAccessible = false;
		try {
			client = bean.getClass().getDeclaredField("delegate");
			oldAccessible = client.isAccessible();
			client.setAccessible(true);
			return (Client) client.get(bean);
		}
		catch (Exception e) {
			LOG.error("get delegate client error", e);
		}
		finally {
			client.setAccessible(oldAccessible);
		}
		return null;
	}

	private BlockingLoadBalancerClient loadBalancerClient() {
		if (this.loadBalancerClient != null) {
			return this.loadBalancerClient;
		}
		return beanFactory.getBean(BlockingLoadBalancerClient.class);
	}

	CachingSpringLoadBalancerFactory factory() {
		if (this.cachingSpringLoadBalancerFactory == null) {
			this.cachingSpringLoadBalancerFactory = this.beanFactory
					.getBean(CachingSpringLoadBalancerFactory.class);
		}
		return this.cachingSpringLoadBalancerFactory;
	}

	SpringClientFactory clientFactory() {
		if (this.springClientFactory == null) {
			this.springClientFactory = this.beanFactory
					.getBean(SpringClientFactory.class);
		}
		return this.springClientFactory;
	}

}
