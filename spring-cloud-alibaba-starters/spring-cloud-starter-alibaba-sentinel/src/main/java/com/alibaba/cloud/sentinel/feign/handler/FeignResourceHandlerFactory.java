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

package com.alibaba.cloud.sentinel.feign.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;

/**
 * @author <a href="a11en.huang@foxmail.com">Allen Huang</a>
 */
public class FeignResourceHandlerFactory implements BeanPostProcessor {

	private static final Map<String, FeignResourceHandler> HANDLER_MAP = new HashMap<>();

	private static FeignResourceHandler DEFAULT_HANDLER = new RestFeignResourceHandler();

	public static FeignResourceHandler getHandler(String strategy) {
		return Optional.ofNullable(HANDLER_MAP.get(strategy)).orElse(DEFAULT_HANDLER);
	}

	public static FeignResourceHandler getDefaultHandler() {
		return DEFAULT_HANDLER;
	}

	public void setDefaultStrategy(FeignResourceHandler defaultHandler) {
		if (defaultHandler != null) {
			DEFAULT_HANDLER = defaultHandler;
		}
	}

	@Override
	public Object postProcessAfterInitialization(@NonNull Object bean,
			@NonNull String beanName) throws BeansException {
		if (bean instanceof FeignResourceHandler) {
			FeignResourceHandler handler = (FeignResourceHandler) bean;
			String type = handler.handlerType();
			synchronized (HANDLER_MAP) {
				FeignResourceHandler initBean = HANDLER_MAP.get(type);
				if (initBean != null && handler != initBean) {
					throw new BeanCreationException("Repeated strategy. type: " + type,
							"," + initBean.getClass().getName() + " <-->"
									+ beanClass(bean));
				}
				HANDLER_MAP.put(type, handler);
			}
		}
		return bean;
	}

	private Class<?> beanClass(Object bean) {
		return AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean)
				: bean.getClass();
	}

}
