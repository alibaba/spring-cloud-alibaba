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

package com.alibaba.cloud.appactive.consumer;

import feign.codec.Decoder;
import io.appactive.support.log.LogUtil;
import org.slf4j.Logger;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
public class FeignDecoderPostProcessor implements BeanPostProcessor {

	private static final Logger logger = LogUtil.getLogger();

	final ApplicationContext context;

	public FeignDecoderPostProcessor(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		// there does`t have to be a Decoder(when using default), so we added a default
		if (bean instanceof Decoder) {
			if ("appActiveFeignDecoder".equals(beanName)) {
				logger.info(
						"FeignDecoderPostProcessor replacing defaultDecoder {} ......",
						beanName);
			}
			else {
				logger.info(
						"FeignDecoderPostProcessor replacing customizedDecoder {} ......",
						beanName);
			}
			Decoder decoder = (Decoder) bean;
			// wrap original decoder
			return new FeignResponseDecoderInterceptor(decoder);

			/// another way
			// Object proxy = Proxy.newProxyInstance(bean.getClass().getClassLoader(),
			// bean.getClass().getInterfaces(),
			// (proxy1, method, args) -> {
			// String result = (String) method.invoke(bean, args);
			// return result.toUpperCase();
			// });
			// return proxy;
		}
		return bean;
	}

}
