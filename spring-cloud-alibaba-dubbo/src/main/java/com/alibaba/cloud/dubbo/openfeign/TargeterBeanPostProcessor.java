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

package com.alibaba.cloud.dubbo.openfeign;

import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContextFactory;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import static com.alibaba.cloud.dubbo.autoconfigure.DubboOpenFeignAutoConfiguration.TARGETER_CLASS_NAME;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.springframework.util.ClassUtils.getUserClass;
import static org.springframework.util.ClassUtils.isPresent;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * org.springframework.cloud.openfeign.Targeter {@link BeanPostProcessor}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class TargeterBeanPostProcessor implements BeanPostProcessor {

	private final Environment environment;

	private final DubboServiceMetadataRepository dubboServiceMetadataRepository;

	private final DubboGenericServiceFactory dubboGenericServiceFactory;

	private final DubboGenericServiceExecutionContextFactory contextFactory;

	private static final ClassLoader CLASS_LOADER = ClassUtils.class.getClassLoader();

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
		if (isPresent(TARGETER_CLASS_NAME, CLASS_LOADER)) {
			Class<?> beanClass = getUserClass(bean.getClass());
			Class<?> targetClass = resolveClassName(TARGETER_CLASS_NAME, CLASS_LOADER);
			if (targetClass.isAssignableFrom(beanClass)) {
				return newProxyInstance(CLASS_LOADER, new Class[] { targetClass },
						new TargeterInvocationHandler(bean, environment, CLASS_LOADER,
								dubboServiceMetadataRepository,
								dubboGenericServiceFactory, contextFactory));
			}
		}
		return bean;
	}

}
