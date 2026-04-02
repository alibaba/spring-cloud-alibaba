/*
 * Copyright 2013-present the original author or authors.
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

package com.alibaba.cloud.sentinel.restclient;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.sentinel.SentinelConstants;
import com.alibaba.cloud.sentinel.annotation.SentinelRestClient;
import com.alibaba.cloud.sentinel.custom.BlockClassRegistry;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * PostProcessor that handles {@link SentinelRestClient} annotation,
 * adding interceptor for {@link RestClient.Builder}.
 *
 * @author QHT, uuuyuqi
 * @see SentinelRestClient
 * @see SentinelRestClientInterceptor
 */
public class SentinelRestClientBeanPostProcessor
		implements MergedBeanDefinitionPostProcessor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SentinelRestClientBeanPostProcessor.class);

	private final ApplicationContext applicationContext;

	private ConcurrentHashMap<String, SentinelRestClient> cache = new ConcurrentHashMap<>();

	public SentinelRestClientBeanPostProcessor(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition,
			Class<?> beanType, String beanName) {
		if (beanName == null || !RestClient.Builder.class.isAssignableFrom(beanType)) {
			return;
		}

		SentinelRestClient sentinelRestClient = getSentinelRestClientFromBeanDefinition(
				beanDefinition);
		if (sentinelRestClient != null) {
			// check class and method validation
			checkSentinelRestClient(sentinelRestClient, beanName);
			cache.put(beanName, sentinelRestClient);
		}
	}

	private SentinelRestClient getSentinelRestClientFromBeanDefinition(
			RootBeanDefinition beanDefinition) {
		SentinelRestClient sentinelRestClient = null;
		if (beanDefinition.getSource() instanceof StandardMethodMetadata sentinelSource) {
			sentinelRestClient = sentinelSource.getIntrospectedMethod()
					.getAnnotation(SentinelRestClient.class);
		}

		if (sentinelRestClient == null
				&& beanDefinition.getResolvedFactoryMethod() != null) {
			sentinelRestClient = beanDefinition.getResolvedFactoryMethod()
					.getAnnotation(SentinelRestClient.class);
		}

		return sentinelRestClient;
	}

	private void checkSentinelRestClient(SentinelRestClient sentinelRestClient,
			String beanName) {
		checkBlock4RestClient(sentinelRestClient.blockHandlerClass(),
				sentinelRestClient.blockHandler(), beanName,
				SentinelConstants.BLOCK_TYPE);
		checkBlock4RestClient(sentinelRestClient.fallbackClass(),
				sentinelRestClient.fallback(), beanName,
				SentinelConstants.FALLBACK_TYPE);
		checkBlock4RestClient(sentinelRestClient.urlCleanerClass(),
				sentinelRestClient.urlCleaner(), beanName,
				SentinelConstants.URLCLEANER_TYPE);
	}

	private void checkBlock4RestClient(Class<?> blockClass, String blockMethod,
			String beanName, String type) {
		if (blockClass == void.class && !StringUtils.hasLength(blockMethod)) {
			return;
		}
		if (blockClass != void.class && !StringUtils.hasLength(blockMethod)) {
			LOGGER.error(
					"{} class attribute exists but {} method attribute is not exists"
							+ " in bean[{}]",
					type, type, beanName);
			throw new IllegalArgumentException(type
					+ " class attribute exists but "
					+ type + " method attribute is not exists in bean[" + beanName + "]");
		}
		else if (blockClass == void.class && StringUtils.hasLength(blockMethod)) {
			LOGGER.error(
					"{} method attribute exists but {} class attribute is not exists"
							+ " in bean[{}]",
					type, type, beanName);
			throw new IllegalArgumentException(type
					+ " method attribute exists but "
					+ type + " class attribute is not exists in bean[" + beanName + "]");
		}
		Class<?>[] args;
		if (type.equals(SentinelConstants.URLCLEANER_TYPE)) {
			args = new Class<?>[] { String.class };
		}
		else {
			args = new Class<?>[] { HttpRequest.class, byte[].class,
					ClientHttpRequestExecution.class, BlockException.class };
		}
		String argsStr = Arrays.toString(
				Arrays.stream(args).map(Class::getSimpleName).toArray(String[]::new));
		Method foundMethod = ClassUtils.getStaticMethod(blockClass, blockMethod, args);
		if (foundMethod == null) {
			LOGGER.error(
					"{} static method can not be found in bean[{}]. "
							+ "The right method signature is {}#{}{}, "
							+ "please check your class name, method name and arguments",
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
			LOGGER.error("{} method return value in bean[{}] is not {}: {}#{}{}", type,
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

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		if (beanName != null && cache.containsKey(beanName)) {
			SentinelRestClient sentinelRestClient = cache.get(beanName);

			StringBuilder interceptorBeanNamePrefix = new StringBuilder();
			interceptorBeanNamePrefix
					.append(StringUtils.uncapitalize(
							SentinelRestClientInterceptor.class.getSimpleName()))
					.append("_")
					.append(sentinelRestClient.blockHandlerClass().getSimpleName())
					.append(sentinelRestClient.blockHandler()).append("_")
					.append(sentinelRestClient.fallbackClass().getSimpleName())
					.append(sentinelRestClient.fallback()).append("_")
					.append(sentinelRestClient.urlCleanerClass().getSimpleName())
					.append(sentinelRestClient.urlCleaner());

			String interceptorBeanName = interceptorBeanNamePrefix + "@"
					+ bean.toString();
			registerBean(interceptorBeanName, sentinelRestClient);
			SentinelRestClientInterceptor interceptor = applicationContext
					.getBean(interceptorBeanName, SentinelRestClientInterceptor.class);

			RestClient.Builder builder = (RestClient.Builder) bean;
			builder.requestInterceptors(list -> {
				if (!list.contains(interceptor)) {
					list.add(0, interceptor); // promote priority (equivalent to addFirst)
				}
			});
		}
		return bean;
	}

	private void registerBean(String interceptorBeanName,
			SentinelRestClient sentinelRestClient) {
		// register SentinelRestClientInterceptor bean
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)
				applicationContext.getAutowireCapableBeanFactory();
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SentinelRestClientInterceptor.class);
		beanDefinitionBuilder.addConstructorArgValue(sentinelRestClient);
		BeanDefinition interceptorBeanDefinition = beanDefinitionBuilder
				.getRawBeanDefinition();
		beanFactory.registerBeanDefinition(interceptorBeanName,
				interceptorBeanDefinition);
	}

}
