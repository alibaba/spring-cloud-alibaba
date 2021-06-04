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

import com.alibaba.csp.sentinel.SphU;
import feign.Feign;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ResourceName handler config.
 *
 * @author Allen Huang
 */
@RefreshScope
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ SphU.class, Feign.class })
@ConfigurationProperties("spring.cloud.sentinel.feign.resource-handler")
public class SentinelResourceHandlerAutoConfiguration {

	/**
	 * Global default resource strategy.
	 */
	private Class<? extends FeignResourceHandler> defaultHandler = RestFeignResourceHandler.class;

	@Bean
	public FeignResourceHandlerFactory feignResourceStrategyFactory()
			throws InstantiationException, IllegalAccessException {
		FeignResourceHandlerFactory resourceHandlerFactory = new FeignResourceHandlerFactory();
		resourceHandlerFactory.setDefaultStrategy(defaultHandler.newInstance());
		return resourceHandlerFactory;
	}

	@Bean
	public RestFeignResourceHandler restFeignResourceHandler() {
		return new RestFeignResourceHandler();
	}

	@Bean
	public ServiceFeignResourceHandler serviceFeignResourceHandler() {
		return new ServiceFeignResourceHandler();
	}

	public Class<? extends FeignResourceHandler> getDefaultHandler() {
		return defaultHandler;
	}

	public void setDefaultHandler(Class<? extends FeignResourceHandler> defaultHandler) {
		if (defaultHandler != null) {
			this.defaultHandler = defaultHandler;
		}
	}

}
