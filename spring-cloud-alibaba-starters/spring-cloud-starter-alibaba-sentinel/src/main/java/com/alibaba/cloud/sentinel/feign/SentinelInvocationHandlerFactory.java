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

package com.alibaba.cloud.sentinel.feign;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import feign.InvocationHandlerFactory;
import feign.Target;
import feign.hystrix.FallbackFactory;

import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author <a href="a11en.huang@foxmail.com">Allen Huang</a>
 */
public class SentinelInvocationHandlerFactory implements InvocationHandlerFactory {

	protected final ApplicationContext applicationContext;

	protected final FeignContext feignContext;

	public SentinelInvocationHandlerFactory(ApplicationContext applicationContext) {
		this(applicationContext, applicationContext.getBean(FeignContext.class));
	}

	public SentinelInvocationHandlerFactory(ApplicationContext applicationContext,
			FeignContext feignContext) {
		this.applicationContext = applicationContext;
		this.feignContext = feignContext;
	}

	@Override
	public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
		// using reflect get fallback and fallbackFactory properties from
		// FeignClientFactoryBean because FeignClientFactoryBean is a package
		// level class, we can not use it in our package
		Object feignClientFactoryBean = applicationContext
				.getBean("&" + target.type().getName());

		Class<?> fallback = (Class<?>) getFieldValue(feignClientFactoryBean, "fallback");
		Class<?> fallbackFactory = (Class<?>) getFieldValue(feignClientFactoryBean,
				"fallbackFactory");
		String beanName = (String) getFieldValue(feignClientFactoryBean, "contextId");
		if (!StringUtils.hasText(beanName)) {
			beanName = (String) getFieldValue(feignClientFactoryBean, "name");
		}

		Object fallbackInstance;
		FallbackFactory<?> fallbackFactoryInstance = null;
		// check fallback and fallbackFactory properties
		if (void.class != fallback) {
			fallbackInstance = getFromContext(beanName, "fallback", fallback,
					target.type());
			fallbackFactoryInstance = new FallbackFactory.Default(fallbackInstance);
		}
		else if (void.class != fallbackFactory) {
			fallbackFactoryInstance = (FallbackFactory) getFromContext(beanName,
					"fallbackFactory", fallbackFactory, FallbackFactory.class);
		}
		return buildInvocationHandler(target, dispatch, fallbackFactoryInstance);
	}

	protected InvocationHandler buildInvocationHandler(Target<?> target,
			Map<Method, MethodHandler> dispatch, FallbackFactory fallbackFactory) {
		return fallbackFactory == null ? new SentinelInvocationHandler(target, dispatch)
				: new SentinelInvocationHandler(target, dispatch, fallbackFactory);
	}

	private Object getFromContext(String name, String type, Class<?> fallbackType,
			Class<?> targetType) {
		Object fallbackInstance = feignContext.getInstance(name, fallbackType);
		if (fallbackInstance == null) {
			throw new IllegalStateException(
					String.format("No %s instance of type %s found for feign client %s",
							type, fallbackType, name));
		}

		if (!targetType.isAssignableFrom(fallbackType)) {
			throw new IllegalStateException(String.format(
					"Incompatible %s instance. Fallback/fallbackFactory of type %s is not assignable to %s for feign client %s",
					type, fallbackType, targetType, name));
		}
		return fallbackInstance;
	}

	private Object getFieldValue(Object instance, String fieldName) {
		Field field = ReflectionUtils.findField(instance.getClass(), fieldName);
		if (field != null) {
			boolean accessible = field.isAccessible();
			try {
				field.setAccessible(true);
				return field.get(instance);
			}
			catch (IllegalAccessException e) {
				// ignore
			}
			finally {
				field.setAccessible(accessible);
			}
		}
		return null;
	}

}
