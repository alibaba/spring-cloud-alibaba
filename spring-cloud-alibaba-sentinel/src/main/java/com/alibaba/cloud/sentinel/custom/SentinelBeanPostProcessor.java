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

package com.alibaba.cloud.sentinel.custom;

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
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.alibaba.cloud.sentinel.SentinelConstants;
import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * PostProcessor handle @SentinelRestTemplate Annotation, add interceptor for RestTemplate
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see SentinelRestTemplate
 * @see SentinelProtectInterceptor
 */
public class SentinelBeanPostProcessor implements MergedBeanDefinitionPostProcessor {

	private static final Logger log = LoggerFactory
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
		checkBlock4RestTemplate(sentinelRestTemplate.urlCleanerClass(),
				sentinelRestTemplate.urlCleaner(), beanName,
				SentinelConstants.URLCLEANER_TYPE);
	}

	private void checkBlock4RestTemplate(Class<?> blockClass, String blockMethod,
			String beanName, String type) {
		if (blockClass == void.class && StringUtils.isEmpty(blockMethod)) {
			return;
		}
		if (blockClass != void.class && StringUtils.isEmpty(blockMethod)) {
			log.error(
					"{} class attribute exists but {} method attribute is not exists in bean[{}]",
					type, type, beanName);
			throw new IllegalArgumentException(type + " class attribute exists but "
					+ type + " method attribute is not exists in bean[" + beanName + "]");
		}
		else if (blockClass == void.class && !StringUtils.isEmpty(blockMethod)) {
			log.error(
					"{} method attribute exists but {} class attribute is not exists in bean[{}]",
					type, type, beanName);
			throw new IllegalArgumentException(type + " method attribute exists but "
					+ type + " class attribute is not exists in bean[" + beanName + "]");
		}
		Class[] args;
		if (type.equals(SentinelConstants.URLCLEANER_TYPE)) {
			args = new Class[] { String.class };
		}
		else {
			args = new Class[] { HttpRequest.class, byte[].class,
					ClientHttpRequestExecution.class, BlockException.class };
		}
		String argsStr = Arrays.toString(
				Arrays.stream(args).map(clazz -> clazz.getSimpleName()).toArray());
		Method foundMethod = ClassUtils.getStaticMethod(blockClass, blockMethod, args);
		if (foundMethod == null) {
			log.error(
					"{} static method can not be found in bean[{}]. The right method signature is {}#{}{}, please check your class name, method name and arguments",
					type, beanName, blockClass.getName(), blockMethod, argsStr);
			throw new IllegalArgumentException(type
					+ " static method can not be found in bean[" + beanName
					+ "]. The right method signature is " + blockClass.getName() + "#"
					+ blockMethod + argsStr
					+ ", please check your class name, method name and arguments");
		}

		Class<?> standardReturnType;
		if (type.equals(SentinelConstants.URLCLEANER_TYPE)) {
			standardReturnType = String.class;
		}
		else {
			standardReturnType = ClientHttpResponse.class;
		}

		if (!standardReturnType.isAssignableFrom(foundMethod.getReturnType())) {
			log.error("{} method return value in bean[{}] is not {}: {}#{}{}", type,
					beanName, standardReturnType.getName(), blockClass.getName(),
					blockMethod, argsStr);
			throw new IllegalArgumentException(type + " method return value in bean["
					+ beanName + "] is not " + standardReturnType.getName() + ": "
					+ blockClass.getName() + "#" + blockMethod + argsStr);
		}
		if (type.equals(SentinelConstants.BLOCK_TYPE)) {
			BlockClassRegistry.updateBlockHandlerFor(blockClass, blockMethod,
					foundMethod);
		}
		else if (type.equals(SentinelConstants.FALLBACK_TYPE)) {
			BlockClassRegistry.updateFallbackFor(blockClass, blockMethod, foundMethod);
		}
		else {
			BlockClassRegistry.updateUrlCleanerFor(blockClass, blockMethod, foundMethod);
		}
	}

	private boolean checkSentinelProtect(RootBeanDefinition beanDefinition,
			Class<?> beanType) {
		return beanType == RestTemplate.class
				&& checkMethodMetadataReadingVisitor(beanDefinition);
	}

	private boolean checkMethodMetadataReadingVisitor(RootBeanDefinition beanDefinition) {
		return beanDefinition.getSource() instanceof MethodMetadata
				&& ((MethodMetadata) beanDefinition.getSource())
						.isAnnotated(SentinelRestTemplate.class.getName());
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		if (cache.containsKey(beanName)) {
			// add interceptor for each RestTemplate with @SentinelRestTemplate annotation
			StringBuilder interceptorBeanNamePrefix = new StringBuilder();
			SentinelRestTemplate sentinelRestTemplate = cache.get(beanName);
			interceptorBeanNamePrefix
					.append(StringUtils.uncapitalize(
							SentinelProtectInterceptor.class.getSimpleName()))
					.append("_")
					.append(sentinelRestTemplate.blockHandlerClass().getSimpleName())
					.append(sentinelRestTemplate.blockHandler()).append("_")
					.append(sentinelRestTemplate.fallbackClass().getSimpleName())
					.append(sentinelRestTemplate.fallback()).append("_")
					.append(sentinelRestTemplate.urlCleanerClass().getSimpleName())
					.append(sentinelRestTemplate.urlCleaner());
			RestTemplate restTemplate = (RestTemplate) bean;
			String interceptorBeanName = interceptorBeanNamePrefix + "@"
					+ bean.toString();
			registerBean(interceptorBeanName, sentinelRestTemplate, (RestTemplate) bean);
			SentinelProtectInterceptor sentinelProtectInterceptor = applicationContext
					.getBean(interceptorBeanName, SentinelProtectInterceptor.class);
			restTemplate.getInterceptors().add(0, sentinelProtectInterceptor);
		}
		return bean;
	}

	private void registerBean(String interceptorBeanName,
			SentinelRestTemplate sentinelRestTemplate, RestTemplate restTemplate) {
		// register SentinelProtectInterceptor bean
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext
				.getAutowireCapableBeanFactory();
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SentinelProtectInterceptor.class);
		beanDefinitionBuilder.addConstructorArgValue(sentinelRestTemplate);
		beanDefinitionBuilder.addConstructorArgValue(restTemplate);
		BeanDefinition interceptorBeanDefinition = beanDefinitionBuilder
				.getRawBeanDefinition();
		beanFactory.registerBeanDefinition(interceptorBeanName,
				interceptorBeanDefinition);
	}

}