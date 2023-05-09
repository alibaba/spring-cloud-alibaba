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

import javax.net.ssl.SSLContext;

import com.alibaba.cloud.governance.istio.sds.CertPair;
import com.alibaba.cloud.mtls.MtlsSslStoreProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class ClientRequestFactoryProvider {

	private static final Logger log = LoggerFactory
			.getLogger(ClientRequestFactoryProvider.class);

	private final MtlsSslStoreProvider mtlsSslStoreProvider;

	public ClientRequestFactoryProvider(MtlsSslStoreProvider mtlsSslStoreProvider) {
		this.mtlsSslStoreProvider = mtlsSslStoreProvider;
	}

	public ClientHttpRequestFactory getFactoryByTemplate(RestTemplate restTemplate, CertPair certPair) {
		try {
			SSLContext sslContext = new SSLContextBuilder()
					.loadKeyMaterial(mtlsSslStoreProvider.getKeyStore(certPair), "".toCharArray())
					.loadTrustMaterial(mtlsSslStoreProvider.getTrustStore(certPair), null)
					.build();
			return getFactoryByTemplate(restTemplate, sslContext);
		}
		catch (Exception e) {
			log.error("Can not get factory by template, class name is {}",
					restTemplate.getRequestFactory().getClass().getName(), e);
		}
		return null;
	}

	public ClientHttpRequestFactory getFactoryByTemplate(RestTemplate restTemplate) {
		try {
			SSLContext sslContext = new SSLContextBuilder()
					.loadKeyMaterial(mtlsSslStoreProvider.getKeyStore(), "".toCharArray())
					.loadTrustMaterial(mtlsSslStoreProvider.getTrustStore(), null)
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
		ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
		if (factory instanceof SimpleClientHttpRequestFactory) {
			return new MtlsSimpleClientHttpRequestFactory(sslContext);
		}
		if (factory instanceof HttpComponentsClientHttpRequestFactory) {
			HttpClient client = HttpClients.custom().setSSLContext(sslContext)
					.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
			return new HttpComponentsClientHttpRequestFactory(client);
		}
		return null;
	}

}
