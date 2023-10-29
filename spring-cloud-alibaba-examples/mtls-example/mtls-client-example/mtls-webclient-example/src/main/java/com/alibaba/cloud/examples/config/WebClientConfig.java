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

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;

import com.alibaba.cloud.mtls.client.MtlsClientSSLContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.http.client.HttpClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Autowired
	MtlsClientSSLContext mtlsClientSSLContext;

	@Bean
	@LoadBalanced
	public WebClient.Builder webClient() {
		SslContext nettySslContext = null;
		try {
			nettySslContext = SslContextBuilder.forClient()
					.trustManager(
							mtlsClientSSLContext.getTrustManagerFactory().orElse(null))
					.keyManager(mtlsClientSSLContext.getKeyManagerFactory().orElse(null))
					.build();
		}
		catch (SSLException e) {
			throw new RuntimeException("Error setting SSL context for WebClient", e);
		}

		SslContext finalNettySslContext = nettySslContext;

		HttpClient httpClient = HttpClient.create().secure(spec -> spec
				.sslContext(finalNettySslContext).handlerConfigurator(sslHandler -> {
					SSLEngine engine = sslHandler.engine();
					SSLParameters sslParameters = engine.getSSLParameters();
					sslParameters.setEndpointIdentificationAlgorithm("");
					engine.setSSLParameters(sslParameters);
				}));

		ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
		return WebClient.builder().clientConnector(connector);
	}

}
