package com.alibaba.demo.nacosdruidexample;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.alibaba.cloud","com.alibaba.demo"})
@SpringBootApplication
public class NacosSpringBootDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosSpringBootDemoApplication.class, args);
	}

}
