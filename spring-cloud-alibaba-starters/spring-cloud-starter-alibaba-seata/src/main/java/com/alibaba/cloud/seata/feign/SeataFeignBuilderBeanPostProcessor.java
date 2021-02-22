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

import feign.Feign;
import feign.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author wang.liang
 * @since 2.2.5
 */
public class SeataFeignBuilderBeanPostProcessor implements BeanPostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SeataFeignBuilderBeanPostProcessor.class);

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Feign.Builder) {
			((Feign.Builder) bean).retryer(Retryer.NEVER_RETRY);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("change the retryer of the bean '{}' to 'Retryer.NEVER_RETRY'", beanName);
			}
		}
		return bean;
	}
}
