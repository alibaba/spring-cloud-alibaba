package com.alibaba.cloud.examples.config;

import com.alibaba.cloud.mtls.client.MtlsClientSSLContext;
import org.apache.http.impl.client.HttpClientBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Autowired
	MtlsClientSSLContext mtlsClientSSLContext;

	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(
				HttpClientBuilder.create().setSSLContext(mtlsClientSSLContext.getSslContext())
						.setSSLHostnameVerifier(mtlsClientSSLContext.getHostnameVerifier())
						.build()));
		return restTemplate;
	}
}
