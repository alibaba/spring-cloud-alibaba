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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.openfeign.FeignContext;

/**
 * @author xiaojing
 */
public class SeataContextBeanPostProcessor implements BeanPostProcessor {

	private final BeanFactory beanFactory;

	private SeataFeignObjectWrapper seataFeignObjectWrapper;

	SeataContextBeanPostProcessor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof FeignContext && !(bean instanceof SeataFeignContext)) {
			return new SeataFeignContext(getSeataFeignObjectWrapper(),
					(FeignContext) bean);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	private SeataFeignObjectWrapper getSeataFeignObjectWrapper() {
		if (this.seataFeignObjectWrapper == null) {
			this.seataFeignObjectWrapper = this.beanFactory
					.getBean(SeataFeignObjectWrapper.class);
		}
		return this.seataFeignObjectWrapper;
	}

}
