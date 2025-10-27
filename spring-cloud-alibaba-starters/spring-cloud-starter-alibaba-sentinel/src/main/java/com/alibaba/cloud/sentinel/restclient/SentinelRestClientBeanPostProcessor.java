/*
 * Copyright 2013-2025 the original author or authors.
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

/**
 * BeanPostProcessor that injects Sentinel interceptor into RestClient.Builder.
 */
public class SentinelRestClientBeanPostProcessor implements BeanPostProcessor {

	private final ClientHttpRequestInterceptor interceptor;

	public SentinelRestClientBeanPostProcessor(ClientHttpRequestInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof RestClient.Builder builder) {
			builder.requestInterceptors(list -> {
				if (!list.contains(interceptor)) {
					list.add(0, interceptor); // 提升优先级（等价 addFirst）
				}
			});
		}
		return bean;
	}
}
