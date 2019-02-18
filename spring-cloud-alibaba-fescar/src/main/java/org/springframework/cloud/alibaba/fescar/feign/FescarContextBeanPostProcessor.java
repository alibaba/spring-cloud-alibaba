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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.openfeign.FeignContext;

/**
 * @author xiaojing
 */
public class FescarContextBeanPostProcessor implements BeanPostProcessor {

	private final BeanFactory beanFactory;
	private FescarFeignObjectWrapper fescarFeignObjectWrapper;

	FescarContextBeanPostProcessor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof FeignContext && !(bean instanceof FescarFeignContext)) {
			return new FescarFeignContext(getFescarFeignObjectWrapper(),
					(FeignContext) bean);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	private FescarFeignObjectWrapper getFescarFeignObjectWrapper() {
		if (this.fescarFeignObjectWrapper == null) {
			this.fescarFeignObjectWrapper = this.beanFactory
					.getBean(FescarFeignObjectWrapper.class);
		}
		return this.fescarFeignObjectWrapper;
	}
}
