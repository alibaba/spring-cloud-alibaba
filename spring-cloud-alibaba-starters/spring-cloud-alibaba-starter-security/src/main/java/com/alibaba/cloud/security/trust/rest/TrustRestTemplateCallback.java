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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;



public class TrustRestTemplateCallback implements ApplicationContextAware {

	private static final Logger log = LoggerFactory.getLogger(TrustRestTemplateCallback.class);

	private ApplicationContext applicationContext;

	private final ClientRequestFactoryProvider clientRequestFactoryProvider;

	public TrustRestTemplateCallback(
			ClientRequestFactoryProvider clientRequestFactoryProvider) {
		this.clientRequestFactoryProvider = clientRequestFactoryProvider;
	}

	public void onUpdateCert() {
		// When the certificate is expired, we refresh the client certificate.
		try {
			if (!validateContext()) {
				return;
			}
			Map<String, RestTemplate> restTemplates = applicationContext
					.getBeansOfType(RestTemplate.class);
			for (RestTemplate restTemplate : restTemplates.values()) {
				restTemplate.setRequestFactory(clientRequestFactoryProvider
						.getFactoryByTemplate(restTemplate));
			}
		}
		catch (BeanCreationException e1) {
			log.warn(
					"Spring is creating the RestTemplate bean, please try to refresh the client certificate later");
		}
		catch (Exception e2) {
			log.error("Failed to refresh RestTemplate", e2);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	private boolean validateContext() {
		if (applicationContext instanceof ConfigurableApplicationContext) {
			ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
			return context.isActive();
		}
		return true;
	}

}
