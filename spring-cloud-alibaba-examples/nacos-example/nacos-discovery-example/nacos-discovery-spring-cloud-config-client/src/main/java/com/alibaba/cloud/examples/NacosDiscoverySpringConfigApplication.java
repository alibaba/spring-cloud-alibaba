package com.alibaba.cloud.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author JevonYang
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NacosDiscoverySpringConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosDiscoverySpringConfigApplication.class, args);
	}

}
