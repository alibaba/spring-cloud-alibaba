/*
 * Copyright 2013-2023 the original author or authors.
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

import com.alibaba.cloud.nacos.registry.NacosRegistration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo-yx
 * @author <a href="1481556636@qq.com"></a>
 */

@EnableDiscoveryClient
@SpringBootApplication
public class ProviderA1Application {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "a1");
		SpringApplication.run(ProviderA1Application.class, args);
	}

	@Autowired
	NacosRegistration nacosRegistration;

	@RestController
	class Controller {

		@GetMapping("/test-a1")
		public String test() {
			String host = nacosRegistration.getHost();
			int port = nacosRegistration.getPort();
			String zone = nacosRegistration.getMetadata().get("zone");
			String region = nacosRegistration.getMetadata().get("region");
			String version = nacosRegistration.getMetadata().get("version");
			return "Route in " + host + ":" + port + ", region: " + region + ", zone: "
					+ zone + ", version: " + version;
		}

	}

}
