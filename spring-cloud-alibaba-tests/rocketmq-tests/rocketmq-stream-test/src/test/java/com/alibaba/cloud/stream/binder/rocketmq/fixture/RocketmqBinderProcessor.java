package com.alibaba.cloud.stream.binder.rocketmq.fixture;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RocketmqBinderProcessor {

	@Bean
	public Function<String, String> uppercaseFunction() {
		return String::toUpperCase;
	}

	public static void main(String[] args) {
		SpringApplication.run(RocketmqBinderProcessor.class, args);
	}
}
