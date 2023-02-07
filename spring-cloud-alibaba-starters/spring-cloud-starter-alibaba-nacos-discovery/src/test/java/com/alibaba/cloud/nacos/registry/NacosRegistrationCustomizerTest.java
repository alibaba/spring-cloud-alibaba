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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.alibaba.nacos.api.NacosFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.MethodProxy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author L.cm
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ NacosFactory.class })
@SpringBootTest(classes = NacosRegistrationCustomizerTest.TestConfig.class,
		properties = { "spring.application.name=myTestService1",
				"spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848" },
		webEnvironment = RANDOM_PORT)
public class NacosRegistrationCustomizerTest {

	@Autowired
	private NacosAutoServiceRegistration nacosAutoServiceRegistration;

	static {
		try {
			Method method = PowerMockito.method(NacosFactory.class, "createNamingService",
					Properties.class);
			MethodProxy.proxy(method, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					return new MockNamingService();
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void contextLoads() throws Exception {
		NacosRegistration registration = nacosAutoServiceRegistration.getRegistration();
		Map<String, String> metadata = registration.getMetadata();
		Assert.assertEquals("test1", metadata.get("test1"));
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
