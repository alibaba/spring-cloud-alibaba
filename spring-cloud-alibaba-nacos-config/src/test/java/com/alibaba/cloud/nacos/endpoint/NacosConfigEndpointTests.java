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

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.cloud.nacos.NacosConfigAutoConfiguration;
import com.alibaba.cloud.nacos.NacosConfigBootstrapConfiguration;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.refresh.NacosRefreshHistory;
import com.alibaba.nacos.client.config.NacosConfigService;

/**
 * @author xiaojing
 */

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ NacosConfigService.class })
@SpringBootTest(classes = NacosConfigEndpointTests.TestConfig.class, properties = {
		"spring.application.name=test-name",
		"spring.cloud.nacos.config.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.config.file-extension=properties" }, webEnvironment = NONE)
public class NacosConfigEndpointTests {

	static {

		try {

			Method method = PowerMockito.method(NacosConfigService.class, "getConfig",
					String.class, String.class, long.class);
			MethodProxy.proxy(method, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {

					if ("test-name.properties".equals(args[0])
							&& "DEFAULT_GROUP".equals(args[1])) {
						return "user.name=hello\nuser.age=12";
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
	private NacosConfigProperties properties;

	@Autowired
	private NacosRefreshHistory refreshHistory;

	@Test
	public void contextLoads() throws Exception {

		checkoutEndpoint();
		checkoutAcmHealthIndicator();

	}

	private void checkoutAcmHealthIndicator() {
		try {
			Builder builder = new Builder();

			NacosConfigHealthIndicator healthIndicator = new NacosConfigHealthIndicator(
					properties, properties.configServiceInstance());
			healthIndicator.doHealthCheck(builder);

			Builder builder1 = new Builder();
			List<String> dataIds = new ArrayList<>();
			dataIds.add("test-name.properties");
			builder1.up().withDetail("dataIds", dataIds);

			Assert.assertTrue(builder.build().equals(builder1.build()));

		}
		catch (Exception ignoreE) {

		}

	}

	private void checkoutEndpoint() throws Exception {
		NacosConfigEndpoint endpoint = new NacosConfigEndpoint(properties,
				refreshHistory);
		Map<String, Object> map = endpoint.invoke();
		assertEquals(map.get("NacosConfigProperties"), properties);
		assertEquals(map.get("RefreshHistory"), refreshHistory.getRecords());
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ NacosConfigEndpointAutoConfiguration.class,
			NacosConfigAutoConfiguration.class, NacosConfigBootstrapConfiguration.class })
	public static class TestConfig {
	}
}
