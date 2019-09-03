package com.alibaba.cloud.examples;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.listener.Listener;

/**
 * @author xiaojing, Jianwei Mao
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

@Component
class SampleRunner implements ApplicationRunner {

	@Value("${user.name}")
	String userName;

	@Value("${user.age:25}")
	int userAge;

	@Autowired
	private NacosConfigProperties nacosConfigProperties;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println(
				String.format("Initial username=%s, userAge=%d", userName, userAge));

		nacosConfigProperties.configServiceInstance().addListener(
				"nacos-config-example.properties", "DEFAULT_GROUP", new Listener() {

					/**
					 * Callback with latest config data.
					 *
					 * For example, config data in Nacos is:
					 *
					 * user.name=Nacos user.age=25
					 *
					 * @param configInfo latest config data for specific dataId in Nacos
					 * server
					 */
					@Override
					public void receiveConfigInfo(String configInfo) {
						Properties properties = new Properties();
						try {
							properties.load(new StringReader(configInfo));
						}
						catch (IOException e) {
							e.printStackTrace();
						}
						System.out.println("config changed: " + properties);
					}

					@Override
					public Executor getExecutor() {
						return null;
					}
				});
	}
}

@RestController
@RefreshScope
class SampleController {

	@Value("${user.name}")
	String userName;

	@Value("${user.age:25}")
	int age;

	@RequestMapping("/user")
	public String simple() {
		return "Hello Nacos Config!" + "Hello " + userName + " " + age + "!";
	}
}