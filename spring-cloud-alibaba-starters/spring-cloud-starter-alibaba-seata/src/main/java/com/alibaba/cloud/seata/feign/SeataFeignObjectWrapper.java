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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

/**
 * @author xiaojing
 * @author alan-tang-tt
 */
public class SeataFeignObjectWrapper {

	private static final Logger LOG = LoggerFactory
			.getLogger(SeataFeignObjectWrapper.class);

	private static final String EXCEPTION_WARNING = "Exception occurred while trying to access the delegate's field. Will fallback to default instrumentation mechanism, which means that the delegate might not be instrumented";

	private static final String DELEGATE = "delegate";

	private final BeanFactory beanFactory;

	private CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory;

	private SpringClientFactory springClientFactory;

	SeataFeignObjectWrapper(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	Object wrap(Object bean) {
		if (bean instanceof Client && !(bean instanceof SeataFeignClient)) {
			if (bean instanceof LoadBalancerFeignClient) {
				return instrumentedLoadBalancerClient(bean);
			}
			if (bean instanceof FeignBlockingLoadBalancerClient) {
				return instrumentedLoadBalancerClient(bean);
			}
			return new SeataFeignClient(this.beanFactory, (Client) bean);
		}
		return bean;
	}

	private Object instrumentedLoadBalancerClient(Object bean) {
		try {
			Field delegate = bean.getClass().getDeclaredField(DELEGATE);
			delegate.setAccessible(true);
			delegate.set(bean, new SeataFeignObjectWrapper(this.beanFactory)
					.wrap(delegate.get(bean)));
		}
		catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException
				| SecurityException e) {
			LOG.warn(EXCEPTION_WARNING, e);
		}
		return bean;
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
