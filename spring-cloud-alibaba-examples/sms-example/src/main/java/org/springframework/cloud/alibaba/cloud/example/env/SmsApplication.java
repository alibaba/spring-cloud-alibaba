package org.springframework.cloud.alibaba.cloud.example.env;

import org.springframework.alicloud.env.extension.ImportExtraConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ImportExtraConfig(name = "/Users/toava/sms.properties")
public class SmsApplication {

	public static void main(String[] args) {

		SpringApplication.run(SmsApplication.class, args);
	}

	@RestController
	public class EnvExtraController{

		@Value("${spring.cloud.alicloud.access-key:deshao}")
		private String ak;

		@GetMapping("/get-ak.do")
		public String getAk(){
			return ak;
		}
	}
}