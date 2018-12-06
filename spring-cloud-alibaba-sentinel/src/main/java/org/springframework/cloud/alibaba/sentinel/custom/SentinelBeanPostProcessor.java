/*
 * Copyright (C) 2018 the original author or authors.
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

package org.springframework.cloud.alibaba.sentinel.custom;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * PostProcessor handle @SentinelRestTemplate Annotation, add interceptor for RestTemplate
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see SentinelRestTemplate
 * @see SentinelProtectInterceptor
 */
public class SentinelBeanPostProcessor implements MergedBeanDefinitionPostProcessor {

	@Autowired
	private ApplicationContext applicationContext;

	private ConcurrentHashMap<String, SentinelRestTemplate> cache = new ConcurrentHashMap<>();

	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition,
			Class<?> beanType, String beanName) {
		if (checkSentinelProtect(beanDefinition, beanType)) {
			SentinelRestTemplate sentinelRestTemplate = ((StandardMethodMetadata) beanDefinition
					.getSource()).getIntrospectedMethod()
							.getAnnotation(SentinelRestTemplate.class);
			cache.put(beanName, sentinelRestTemplate);
		}
	}

	private boolean checkSentinelProtect(RootBeanDefinition beanDefinition,
			Class<?> beanType) {
		return beanType == RestTemplate.class
				&& beanDefinition.getSource() instanceof StandardMethodMetadata
				&& ((StandardMethodMetadata) beanDefinition.getSource())
						.isAnnotated(SentinelRestTemplate.class.getName());
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		if (cache.containsKey(beanName)) {
			// add interceptor for each RestTemplate with @SentinelRestTemplate annotation
			StringBuilder interceptorBeanName = new StringBuilder();
			SentinelRestTemplate sentinelRestTemplate = cache.get(beanName);
			interceptorBeanName
					.append(StringUtils.uncapitalize(
							SentinelProtectInterceptor.class.getSimpleName()))
					.append("_")
					.append(sentinelRestTemplate.blockHandlerClass().getSimpleName())
					.append(sentinelRestTemplate.blockHandler()).append("_")
					.append(sentinelRestTemplate.fallbackClass().getSimpleName())
					.append(sentinelRestTemplate.fallback());
			RestTemplate restTemplate = (RestTemplate) bean;
			registerBean(interceptorBeanName.toString(), sentinelRestTemplate);
			SentinelProtectInterceptor sentinelProtectInterceptor = applicationContext
					.getBean(interceptorBeanName.toString(),
							SentinelProtectInterceptor.class);
			restTemplate.getInterceptors().add(sentinelProtectInterceptor);
		}
		return bean;
	}

	private void registerBean(String interceptorBeanName,
			SentinelRestTemplate sentinelRestTemplate) {
		// register SentinelProtectInterceptor bean
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext
				.getAutowireCapableBeanFactory();
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SentinelProtectInterceptor.class);
		beanDefinitionBuilder.addConstructorArgValue(sentinelRestTemplate);
		BeanDefinition interceptorBeanDefinition = beanDefinitionBuilder
				.getRawBeanDefinition();
		beanFactory.registerBeanDefinition(interceptorBeanName,
				interceptorBeanDefinition);
	}

}