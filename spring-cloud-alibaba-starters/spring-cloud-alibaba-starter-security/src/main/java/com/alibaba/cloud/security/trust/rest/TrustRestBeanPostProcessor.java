/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.security.trust.rest;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.client.RestTemplate;

public class TrustRestBeanPostProcessor implements BeanPostProcessor {

	private final ClientRequestFactoryProvider clientRequestFactoryProvider;

	public TrustRestBeanPostProcessor(
			ClientRequestFactoryProvider clientRequestFactoryProvider) {
		this.clientRequestFactoryProvider = clientRequestFactoryProvider;
	}


	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		if (bean instanceof RestTemplate) {
			RestTemplate restTemplate = (RestTemplate) bean;
			try {
				restTemplate.setRequestFactory(
						clientRequestFactoryProvider.getFactoryByTemplate(restTemplate));
			}
			catch (Exception e) {
				throw new RuntimeException("Error in enhancing restTemplate", e);
			}
		}
		return bean;
	}

}
