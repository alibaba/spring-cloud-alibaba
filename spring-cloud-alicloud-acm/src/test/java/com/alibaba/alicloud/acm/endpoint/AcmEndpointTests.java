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

package com.alibaba.alicloud.acm.endpoint;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.alicloud.acm.AcmAutoConfiguration;
import com.alibaba.alicloud.acm.AcmPropertySourceRepository;
import com.alibaba.alicloud.acm.refresh.AcmRefreshHistory;
import com.alibaba.alicloud.context.acm.AcmContextBootstrapConfiguration;
import com.alibaba.alicloud.context.acm.AcmProperties;
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
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * @author xiaojing
 */

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ ConfigService.class })
@SpringBootTest(classes = AcmEndpointTests.TestConfig.class,
		properties = { "spring.application.name=test-name",
				"spring.cloud.alicloud.acm.server-list=127.0.0.1",
				"spring.cloud.alicloud.acm.server-port=8848",
				"spring.cloud.alicloud.acm.file-extension=properties" },
		webEnvironment = NONE)
public class AcmEndpointTests {

	static {

		try {

			Method method = PowerMockito.method(ConfigService.class, "getConfig",
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
	private AcmProperties properties;

	@Autowired
	private AcmRefreshHistory refreshHistory;

	@Autowired
	private AcmPropertySourceRepository propertySourceRepository;

	@Autowired
	private AcmPropertySourceRepository acmPropertySourceRepository;

	@Test
	public void contextLoads() throws Exception {

		checkoutEndpoint();
		checkoutAcmHealthIndicator();

	}

	private void checkoutAcmHealthIndicator() {
		try {
			Builder builder = new Builder();

			AcmHealthIndicator healthIndicator = new AcmHealthIndicator(properties,
					acmPropertySourceRepository);
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
		AcmEndpoint acmEndpoint = new AcmEndpoint(properties, refreshHistory,
				propertySourceRepository);
		Map<String, Object> map = acmEndpoint.invoke();
		assertThat(properties).isEqualTo(map.get("config"));
		assertThat(refreshHistory.getRecords())
				.isEqualTo(((Map) map.get("runtime")).get("refreshHistory"));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AcmEndpointAutoConfiguration.class,
			AcmAutoConfiguration.class, AcmContextBootstrapConfiguration.class })
	public static class TestConfig {

	}

}
