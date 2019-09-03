/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.endpoint;

import com.alibaba.nacos.client.naming.net.NamingProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.MethodProxy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ NamingProxy.class })
@SpringBootTest(classes = NacosDiscoveryEndpointTests.TestConfig.class, properties = {
		"spring.application.name=test-name",
		"spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848", }, webEnvironment = NONE)
public class NacosDiscoveryEndpointTests {

	static {

		try {

			Method method = PowerMockito.method(NamingProxy.class, "serverHealthy");
			MethodProxy.proxy(method, (proxy, method1, args) -> true);

		}
		catch (Exception ignore) {
		}
	}

	@Autowired
	private NacosDiscoveryHealthIndicator healthIndicator;

	@Test
	public void checkoutNacosHealthIndicator() {
		try {
			Builder builder = new Builder();
			healthIndicator.doHealthCheck(builder);

			Builder builder1 = new Builder();
			builder1.up();

			assertEquals(builder1.build(), builder.build());
		}
		catch (Exception ignore) {

		}
	}

	@Configuration
	@EnableAutoConfiguration
	public static class TestConfig {
	}
}
