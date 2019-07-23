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

package com.alibaba.alicloud.acm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.MethodProxy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.alicloud.acm.bootstrap.AcmPropertySourceLocator;
import com.alibaba.alicloud.acm.endpoint.AcmEndpointAutoConfiguration;
import com.alibaba.alicloud.context.acm.AcmContextBootstrapConfiguration;
import com.alibaba.alicloud.context.acm.AcmIntegrationProperties;
import com.alibaba.alicloud.context.acm.AcmProperties;
import com.alibaba.edas.acm.ConfigService;

/**
 * @author xiaojing
 */

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ ConfigService.class })
@SpringBootTest(classes = AcmConfigurationTests.TestConfig.class, properties = {
		"spring.application.name=test-name", "spring.profiles.active=dev,test",
		"spring.cloud.alicloud.acm.server-list=127.0.0.1",
		"spring.cloud.alicloud.acm.server-port=8848",
		"spring.cloud.alicloud.acm.endpoint=test-endpoint",
		"spring.cloud.alicloud.acm.namespace=test-namespace",
		"spring.cloud.alicloud.acm.timeout=1000",
		"spring.cloud.alicloud.acm.group=test-group",
		"spring.cloud.alicloud.acm.refresh-enabled=false",
		"spring.cloud.alicloud.acm.file-extension=properties" }, webEnvironment = NONE)
public class AcmConfigurationTests {

	static {

		try {

			Method method = PowerMockito.method(ConfigService.class, "getConfig",
					String.class, String.class, long.class);
			MethodProxy.proxy(method, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {

					if ("test-name.properties".equals(args[0])
							&& "test-group".equals(args[1])) {
						return "user.name=hello\nuser.age=12";
					}

					if ("test-name-dev.properties".equals(args[0])
							&& "test-group".equals(args[1])) {
						return "user.name=dev";
					}
					return "";
				}
			});

		}
		catch (Exception ignore) {
			ignore.printStackTrace();

		}
	}

	@Autowired
	private Environment environment;

	@Autowired
	private AcmPropertySourceLocator locator;

	@Autowired
	private AcmIntegrationProperties integrationProperties;

	@Autowired
	private AcmProperties properties;

	@Test
	public void contextLoads() throws Exception {

		assertNotNull("AcmPropertySourceLocator was not created", locator);
		assertNotNull("AcmProperties was not created", properties);
		assertNotNull("AcmIntegrationProperties was not created", integrationProperties);

		checkoutAcmServerAddr();
		checkoutAcmServerPort();
		checkoutAcmEndpoint();
		checkoutAcmNamespace();
		checkoutAcmGroup();
		checkoutAcmFileExtension();
		checkoutAcmTimeout();
		checkoutAcmProfiles();
		checkoutAcmRefreshEnabled();
		checkoutDataLoad();
		checkoutProfileDataLoad();
	}

	private void checkoutAcmServerAddr() {
		assertEquals("AcmProperties server address is wrong", "127.0.0.1",
				properties.getServerList());

	}

	private void checkoutAcmServerPort() {
		assertEquals("AcmProperties server port is wrong", "8848",
				properties.getServerPort());

	}

	private void checkoutAcmEndpoint() {
		assertEquals("AcmProperties endpoint is wrong", "test-endpoint",
				properties.getEndpoint());

	}

	private void checkoutAcmNamespace() {
		assertEquals("AcmProperties namespace is wrong", "test-namespace",
				properties.getNamespace());

	}

	private void checkoutAcmGroup() {
		assertEquals("AcmProperties' group is wrong", "test-group",
				properties.getGroup());
	}

	private void checkoutAcmFileExtension() {
		assertEquals("AcmProperties' file extension is wrong", "properties",
				properties.getFileExtension());
	}

	private void checkoutAcmTimeout() {
		assertEquals("AcmProperties' timeout is wrong", 1000, properties.getTimeout());
	}

	private void checkoutAcmRefreshEnabled() {
		assertEquals("AcmProperties' refresh enabled is wrong", false,
				properties.isRefreshEnabled());
	}

	private void checkoutAcmProfiles() {
		assertArrayEquals("AcmProperties' profiles is wrong",
				new String[] { "dev", "test" },
				integrationProperties.getActiveProfiles());
	}

	private void checkoutDataLoad() {
		Assert.assertEquals(environment.getProperty("user.age"), "12");
	}

	private void checkoutProfileDataLoad() {
		Assert.assertEquals(environment.getProperty("user.name"), "dev");
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AcmEndpointAutoConfiguration.class,
			AcmAutoConfiguration.class, AcmContextBootstrapConfiguration.class })
	public static class TestConfig {
	}
}
