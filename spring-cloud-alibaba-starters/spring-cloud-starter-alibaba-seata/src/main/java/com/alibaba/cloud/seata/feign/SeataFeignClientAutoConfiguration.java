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

package com.alibaba.cloud.seata.feign;

import feign.Client;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaojing
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Client.class)
@AutoConfigureBefore(FeignAutoConfiguration.class)
public class SeataFeignClientAutoConfiguration {

	@Bean
	@ConditionalOnClass(RequestInterceptor.class)
	public SeataFeignRequestInterceptor seataFeignInterceptor() {
		return new SeataFeignRequestInterceptor();
	}
}
