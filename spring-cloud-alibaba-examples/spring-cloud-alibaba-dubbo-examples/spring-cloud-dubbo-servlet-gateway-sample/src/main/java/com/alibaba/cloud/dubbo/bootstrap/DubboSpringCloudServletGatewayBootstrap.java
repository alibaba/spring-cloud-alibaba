package com.alibaba.cloud.dubbo.bootstrap;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Dubbo Spring Cloud Servlet Gateway Bootstrap
 */
@EnableDiscoveryClient
@EnableAutoConfiguration
@EnableFeignClients
@ServletComponentScan(basePackages = "com.alibaba.cloud.dubbo.gateway")
public class DubboSpringCloudServletGatewayBootstrap {

	public static void main(String[] args) {
		new SpringApplicationBuilder(DubboSpringCloudServletGatewayBootstrap.class)
				.properties("spring.profiles.active=nacos").run(args);
	}
}
