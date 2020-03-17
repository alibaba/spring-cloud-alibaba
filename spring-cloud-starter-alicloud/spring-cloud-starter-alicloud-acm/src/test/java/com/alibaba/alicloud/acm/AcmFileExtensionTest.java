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

package com.alibaba.alicloud.acm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.alibaba.alicloud.acm.endpoint.AcmEndpointAutoConfiguration;
import com.alibaba.alicloud.context.acm.AcmContextBootstrapConfiguration;
import com.alibaba.edas.acm.ConfigService;
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

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * @author xiaojing
 */

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ ConfigService.class })
@SpringBootTest(classes = AcmFileExtensionTest.TestConfig.class,
		properties = { "spring.application.name=test-name",
				"spring.cloud.alicloud.acm.server-list=127.0.0.1",
				"spring.cloud.alicloud.acm.server-port=8080",
				"spring.cloud.alicloud.acm.file-extension=yaml" },
		webEnvironment = NONE)
public class AcmFileExtensionTest {

	static {

		try {
			Method method = PowerMockito.method(ConfigService.class, "getConfig",
					String.class, String.class, long.class);
			MethodProxy.proxy(method, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					if ("test-name.yaml".equals(args[0])
							&& "DEFAULT_GROUP".equals(args[1])) {
						return "user:\n  name: hello\n  age: 12";
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

	@Test
	public void contextLoads() throws Exception {

		Assert.assertEquals(environment.getProperty("user.name"), "hello");
		Assert.assertEquals(environment.getProperty("user.age"), "12");
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AcmEndpointAutoConfiguration.class,
			AcmAutoConfiguration.class, AcmContextBootstrapConfiguration.class })
	public static class TestConfig {

	}

}
