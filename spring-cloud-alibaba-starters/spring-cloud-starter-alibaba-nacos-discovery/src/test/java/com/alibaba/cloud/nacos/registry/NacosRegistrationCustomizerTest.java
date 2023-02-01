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

package com.alibaba.cloud.nacos.registry;

import java.util.Map;
import java.util.Properties;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.alibaba.nacos.api.NacosFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author L.cm
 */
@SpringBootTest(classes = NacosRegistrationCustomizerTest.TestConfig.class,
		properties = { "spring.application.name=myTestService1",
				"spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848" },
		webEnvironment = RANDOM_PORT)
public class NacosRegistrationCustomizerTest {

	@Autowired
	private NacosAutoServiceRegistration nacosAutoServiceRegistration;

	private static MockedStatic<NacosFactory> nacosFactoryMockedStatic;
	static {
		nacosFactoryMockedStatic = Mockito.mockStatic(NacosFactory.class);
		nacosFactoryMockedStatic.when(() -> NacosFactory.createNamingService((Properties) any()))
				.thenReturn(new MockNamingService());
	}
	@AfterAll
	public static void finished() {
		if (nacosFactoryMockedStatic != null) {
			nacosFactoryMockedStatic.close();
		}
	}


	@Test
	public void contextLoads() throws Exception {
		NacosRegistration registration = nacosAutoServiceRegistration.getRegistration();
		Map<String, String> metadata = registration.getMetadata();
		Assertions.assertEquals("test1", metadata.get("test1"));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			NacosDiscoveryClientConfiguration.class,
			NacosServiceRegistryAutoConfiguration.class })
	public static class TestConfig {

		@Bean
		public NacosRegistrationCustomizer nacosRegistrationCustomizer() {
			return registration -> {
				Map<String, String> metadata = registration.getMetadata();
				metadata.put("test1", "test1");
			};
		}

	}

}
