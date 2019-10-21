/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.examples;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@NacosPropertySource(dataId = "nacos_cloud_boot", autoRefreshed = true)
class SampleRunner implements ApplicationRunner {

	@Value("${user.name}")
	String userName;

	@Value("${user.age:25}")
	int userAge;

	@NacosInjected
	private ConfigService configService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println(
				String.format("Initial username=%s, userAge=%d", userName, userAge));

		configService.addListener("nacos-config-example.properties", "DEFAULT_GROUP",
				new Listener() {

					/**
					 * Callback with latest config data.
					 *
					 * For example, config data in Nacos is:
					 *
					 * user.name=Nacos user.age=25
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
	Integer age;

	@NacosValue(value = "${nacos.test.value}", autoRefreshed = true)
	String testValue;

	@NacosInjected
	private ConfigService configService;

	@RequestMapping("/user")
	public String simple() {
		return "Hello Nacos Config!" + "Hello " + userName + " " + age + " " + testValue
				+ "!" + configService;
	}

}
