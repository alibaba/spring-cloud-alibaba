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

package com.alibaba.cloud.security.trust.rest;

import javax.net.ssl.SSLContext;

import com.alibaba.cloud.security.trust.TrustSslStoreProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;




public class ClientRequestFactoryProvider {

	private static final Logger log = LoggerFactory
			.getLogger(ClientRequestFactoryProvider.class);

	private final TrustSslStoreProvider trustSslStoreProvider;

	public ClientRequestFactoryProvider(TrustSslStoreProvider trustSslStoreProvider) {
		this.trustSslStoreProvider = trustSslStoreProvider;
	}


	public ClientHttpRequestFactory getFactoryByTemplate(RestTemplate restTemplate) {
		try {
			SSLContext sslContext = new SSLContextBuilder()
					.loadKeyMaterial(trustSslStoreProvider.getKeyStore(), "".toCharArray())
					.loadTrustMaterial(trustSslStoreProvider.getTrustStore(), null)
					.build();
			return getFactoryByTemplate(restTemplate, sslContext);
		}
		catch (Exception e) {
			log.error("Can not get factory by template, class name is {}",
					restTemplate.getRequestFactory().getClass().getName(), e);
		}
		return null;
	}

	private ClientHttpRequestFactory getFactoryByTemplate(RestTemplate restTemplate, SSLContext sslContext) {
		return new TrustSimpleClientHttpRequestFactory(sslContext);
	}

}
