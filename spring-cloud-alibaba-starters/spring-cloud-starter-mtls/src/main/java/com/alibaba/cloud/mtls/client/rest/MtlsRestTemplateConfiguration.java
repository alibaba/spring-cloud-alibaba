/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.mtls.client.rest;

import com.alibaba.cloud.mtls.MtlsSslStoreProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MtlsRestTemplateConfiguration {

	@Bean
	public ClientRequestFactoryProvider clientRequestFactoryProvider(
			MtlsSslStoreProvider mtlsSslStoreProvider) {
		return new ClientRequestFactoryProvider(mtlsSslStoreProvider);
	}

	@Bean
	public RestMtlsBeanPostProcessor restMtlsBeanPostProcessor(
			ClientRequestFactoryProvider clientRequestFactoryProvider) {
		return new RestMtlsBeanPostProcessor(clientRequestFactoryProvider);
	}

}
