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
					.trustManager(mtlsClientSSLContext.getTrustManagerFactory().orElse(null))
					.keyManager(mtlsClientSSLContext.getKeyManagerFactory().orElse(null))
					.build();
		}
		catch (SSLException e) {
			throw new RuntimeException("Error setting SSL context for WebClient", e);
		}

		SslContext finalNettySslContext = nettySslContext;

		HttpClient httpClient = HttpClient.create()
				.secure(spec -> spec.sslContext(finalNettySslContext).handlerConfigurator(sslHandler -> {
					SSLEngine engine = sslHandler.engine();
					SSLParameters sslParameters = engine.getSSLParameters();
					sslParameters.setEndpointIdentificationAlgorithm("");
					engine.setSSLParameters(sslParameters);
				}));

		ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
		return WebClient.builder().clientConnector(connector);
	}

}
