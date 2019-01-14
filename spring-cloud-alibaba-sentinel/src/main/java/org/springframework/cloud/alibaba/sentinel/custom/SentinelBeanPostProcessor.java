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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.cloud.alibaba.sentinel.SentinelConstants;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.core.type.classreading.MethodMetadataReadingVisitor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * PostProcessor handle @SentinelRestTemplate Annotation, add interceptor for RestTemplate
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see SentinelRestTemplate
 * @see SentinelProtectInterceptor
 */
public class SentinelBeanPostProcessor implements MergedBeanDefinitionPostProcessor {

	private static final Logger logger = LoggerFactory
			.getLogger(SentinelBeanPostProcessor.class);

	private final ApplicationContext applicationContext;

	public SentinelBeanPostProcessor(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	private ConcurrentHashMap<String, SentinelRestTemplate> cache = new ConcurrentHashMap<>();

	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition,
			Class<?> beanType, String beanName) {
		if (checkSentinelProtect(beanDefinition, beanType)) {
			SentinelRestTemplate sentinelRestTemplate;
			if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
				sentinelRestTemplate = ((StandardMethodMetadata) beanDefinition
						.getSource()).getIntrospectedMethod()
								.getAnnotation(SentinelRestTemplate.class);
			}
			else {
				sentinelRestTemplate = beanDefinition.getResolvedFactoryMethod()
						.getAnnotation(SentinelRestTemplate.class);
			}
			// check class and method validation
			checkSentinelRestTemplate(sentinelRestTemplate, beanName);
			cache.put(beanName, sentinelRestTemplate);
		}
	}

	private void checkSentinelRestTemplate(SentinelRestTemplate sentinelRestTemplate,
			String beanName) {
		checkBlock4RestTemplate(sentinelRestTemplate.blockHandlerClass(),
				sentinelRestTemplate.blockHandler(), beanName,
				SentinelConstants.BLOCK_TYPE);
		checkBlock4RestTemplate(sentinelRestTemplate.fallbackClass(),
				sentinelRestTemplate.fallback(), beanName,
				SentinelConstants.FALLBACK_TYPE);
	}

	private void checkBlock4RestTemplate(Class<?> blockClass, String blockMethod,
			String beanName, String type) {
		if (blockClass == void.class && StringUtils.isEmpty(blockMethod)) {
			return;
		}
		if (blockClass != void.class && StringUtils.isEmpty(blockMethod)) {
			logger.error(
					"{} class property exists but {}"
							+ " method property is not exists in bean[{}]",
					type, type, beanName);
			System.exit(-1);
		}
		else if (blockClass == void.class && !StringUtils.isEmpty(blockMethod)) {
			logger.error(
					"{} method property exists but {} class property is not exists in bean[{}]",
					type, type, beanName);
			System.exit(-1);
		}
		Class[] args = new Class[] { HttpRequest.class, byte[].class,
				ClientHttpRequestExecution.class, BlockException.class };
		Method foundMethod = ClassUtils.getStaticMethod(blockClass, blockMethod, args);
		if (foundMethod == null) {
			logger.error(
					"{} method can not be found in bean[{}]. The right method signature is {}#{}{}, please check your class name, method name and arguments",
					type, beanName, blockClass.getName(), blockMethod,
					Arrays.toString(Arrays.stream(args)
							.map(clazz -> clazz.getSimpleName()).toArray()));
			System.exit(-1);
		}

		if (!ClientHttpResponse.class.isAssignableFrom(foundMethod.getReturnType())) {
			logger.error(
					"{} method return value in bean[{}] is not ClientHttpResponse: {}#{}{}",
					type, beanName, blockClass.getName(), blockMethod,
					Arrays.toString(Arrays.stream(args)
							.map(clazz -> clazz.getSimpleName()).toArray()));
			System.exit(-1);
		}
		if (type.equals(SentinelConstants.BLOCK_TYPE)) {
			BlockClassRegistry.updateBlockHandlerFor(blockClass, blockMethod,
					foundMethod);
		}
		else {
			BlockClassRegistry.updateFallbackFor(blockClass, blockMethod, foundMethod);
		}
	}

	private boolean checkSentinelProtect(RootBeanDefinition beanDefinition,
			Class<?> beanType) {
		return beanType == RestTemplate.class
				&& (checkStandardMethodMetadata(beanDefinition)
						|| checkMethodMetadataReadingVisitor(beanDefinition));
	}

	private boolean checkStandardMethodMetadata(RootBeanDefinition beanDefinition) {
		return beanDefinition.getSource() instanceof StandardMethodMetadata
				&& ((StandardMethodMetadata) beanDefinition.getSource())
						.isAnnotated(SentinelRestTemplate.class.getName());
	}

	private boolean checkMethodMetadataReadingVisitor(RootBeanDefinition beanDefinition) {
		return beanDefinition.getSource() instanceof MethodMetadataReadingVisitor
				&& ((MethodMetadataReadingVisitor) beanDefinition.getSource())
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
			restTemplate.getInterceptors().add(0, sentinelProtectInterceptor);
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