package com.alibaba.cloud.examples.feignclient;

import com.alibaba.cloud.examples.config.FeignClientConfiguration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "https://mtls-webflux-example", configuration = FeignClientConfiguration.class)
public interface WebfluxClient {
	@GetMapping("/webflux/get")
	String getWebflux();
}
