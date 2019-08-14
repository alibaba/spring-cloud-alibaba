/*
 * Copyright (C) 2018 the original author or authors.
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
package com.alibaba.cloud.dubbo.openfeign;

import static com.alibaba.cloud.dubbo.autoconfigure.DubboOpenFeignAutoConfiguration.TARGETER_CLASS_NAME;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.springframework.util.ClassUtils.getUserClass;
import static org.springframework.util.ClassUtils.isPresent;
import static org.springframework.util.ClassUtils.resolveClassName;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;

import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContextFactory;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceFactory;

/**
 * org.springframework.cloud.openfeign.Targeter {@link BeanPostProcessor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class TargeterBeanPostProcessor
		implements BeanPostProcessor, BeanClassLoaderAware {

	private final Environment environment;

	private final DubboServiceMetadataRepository dubboServiceMetadataRepository;

	private final DubboGenericServiceFactory dubboGenericServiceFactory;

	private final DubboGenericServiceExecutionContextFactory contextFactory;

	private ClassLoader classLoader;

	public TargeterBeanPostProcessor(Environment environment,
			DubboServiceMetadataRepository dubboServiceMetadataRepository,
			DubboGenericServiceFactory dubboGenericServiceFactory,
			DubboGenericServiceExecutionContextFactory contextFactory) {
		this.environment = environment;
		this.dubboServiceMetadataRepository = dubboServiceMetadataRepository;
		this.dubboGenericServiceFactory = dubboGenericServiceFactory;
		this.contextFactory = contextFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName)
			throws BeansException {
		if (isPresent(TARGETER_CLASS_NAME, classLoader)) {
			Class<?> beanClass = getUserClass(bean.getClass());
			Class<?> targetClass = resolveClassName(TARGETER_CLASS_NAME, classLoader);
			if (targetClass.isAssignableFrom(beanClass)) {
				return newProxyInstance(classLoader, new Class[] { targetClass },
						new TargeterInvocationHandler(bean, environment, classLoader,
								dubboServiceMetadataRepository,
								dubboGenericServiceFactory, contextFactory));
			}
		}
		return bean;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
}