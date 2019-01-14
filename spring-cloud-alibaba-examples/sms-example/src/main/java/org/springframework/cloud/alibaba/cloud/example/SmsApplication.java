package org.springframework.cloud.alibaba.cloud.example;

import org.springframework.alicloud.env.extension.ImportExtraConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ImportExtraConfig(name = "/Users/toava/sms.properties")
public class SmsApplication {

	public static void main(String[] args) {

		SpringApplication.run(SmsApplication.class, args);
	}
}