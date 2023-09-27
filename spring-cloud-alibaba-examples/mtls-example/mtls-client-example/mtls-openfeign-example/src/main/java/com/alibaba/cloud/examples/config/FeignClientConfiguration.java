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
	public Client feignClient(CachingSpringLoadBalancerFactory lbClientFactory, SpringClientFactory clientFactory) {
		return new LoadBalancerFeignClient(new feign.okhttp.OkHttpClient(
				new OkHttpClient.Builder()
						.sslSocketFactory(mtlsClientSSLContext.getSslSocketFactory(), mtlsClientSSLContext.getTrustManager().orElse(null))
						.hostnameVerifier(mtlsClientSSLContext.getHostnameVerifier())
						.build()
		), lbClientFactory, clientFactory);
	}

}
