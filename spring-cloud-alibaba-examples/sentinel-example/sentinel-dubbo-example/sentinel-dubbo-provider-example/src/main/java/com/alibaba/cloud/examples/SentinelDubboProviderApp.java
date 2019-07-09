package com.alibaba.cloud.examples;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author fangjian
 */
@SpringBootApplication
public class SentinelDubboProviderApp {

	public static void main(String[] args) {
		SpringApplicationBuilder providerBuilder = new SpringApplicationBuilder();
		providerBuilder.web(false)
				.sources(SentinelDubboProviderApp.class).run(args);
	}

}
