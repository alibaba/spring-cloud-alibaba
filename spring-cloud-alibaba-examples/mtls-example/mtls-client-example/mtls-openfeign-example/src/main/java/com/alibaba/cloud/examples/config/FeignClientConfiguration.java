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

package com.alibaba.cloud.examples.config;

import com.alibaba.cloud.mtls.client.MtlsClientSSLContext;
import feign.Client;
import okhttp3.OkHttpClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfiguration {

	@Autowired
	MtlsClientSSLContext mtlsClientSSLContext;

	@Bean
	public Client feignClient(CachingSpringLoadBalancerFactory lbClientFactory,
			SpringClientFactory clientFactory) {
		return new LoadBalancerFeignClient(
				new feign.okhttp.OkHttpClient(new OkHttpClient.Builder()
						.sslSocketFactory(mtlsClientSSLContext.getSslSocketFactory(),
								mtlsClientSSLContext.getTrustManager().orElse(null))
						.hostnameVerifier(mtlsClientSSLContext.getHostnameVerifier())
						.build()),
				lbClientFactory, clientFactory);
	}

}
