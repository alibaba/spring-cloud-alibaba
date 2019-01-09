//package org.springframework.cloud.alibaba.cloud.example;
//
//import org.springframework.alicloud.env.extension.ImportExtraConfig;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.SpringApplicationRunListener;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.core.env.ConfigurableEnvironment;
//
//@SpringBootApplication
//@ImportExtraConfig(name = "/Users/toava/sms.properties")
//public class SmsApplication {
//
//	public static void main(String[] args) {
//
//		SpringApplication.run(SmsApplication.class, args);
//	}
//
//	public class EvnExtra implements SpringApplicationRunListener {
//		@Override
//		public void starting() {
//
//		}
//
//		@Override
//		public void environmentPrepared(ConfigurableEnvironment environment) {
//
//		}
//
//		@Override
//		public void contextPrepared(ConfigurableApplicationContext context) {
//
//		}
//
//		@Override
//		public void contextLoaded(ConfigurableApplicationContext context) {
//
//		}
//
//		@Override
//		public void finished(ConfigurableApplicationContext context,
//				Throwable exception) {
//			String ak = context.getEnvironment()
//					.getProperty("spring.cloud.alicloud.access-key");
//			System.err.println(ak);
//		}
//	}
//}