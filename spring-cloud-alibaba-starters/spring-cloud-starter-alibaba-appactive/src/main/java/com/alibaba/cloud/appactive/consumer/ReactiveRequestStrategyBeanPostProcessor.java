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

import javax.validation.constraints.NotNull;

import com.alibaba.cloud.appactive.constant.AppactiveConstants;
import io.appactive.java.api.base.AppContextClient;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author yuluo
 */
public class ReactiveRequestStrategyBeanPostProcessor implements BeanPostProcessor {

	final ApplicationContext applicationContext;

	public ReactiveRequestStrategyBeanPostProcessor(
			ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(@NotNull Object bean,
			@NotNull String beanName) {
		if (bean instanceof WebClient || bean instanceof WebClient.Builder) {
			assert bean instanceof WebClient;
			WebClient webClient = (WebClient) bean;

			// add filter
			webClient.mutate().filter((request, next) -> {
				ClientRequest clientRequest = ClientRequest.from(request)
						.headers(headers -> headers.set(
								AppactiveConstants.ROUTER_ID_HEADER_KEY,
								AppContextClient.getRouteId()))
						.build();
				return next.exchange(clientRequest);
			}).build();
		}

		return bean;

	}

}
