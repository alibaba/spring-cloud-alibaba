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

package com.alibaba.cloud.nacos.endpoint;

import java.util.Map;

import com.alibaba.cloud.nacos.NacosConfigAutoConfiguration;
import com.alibaba.cloud.nacos.NacosConfigBootstrapConfiguration;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.refresh.NacosRefreshHistory;
import com.alibaba.nacos.client.config.NacosConfigService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 *
 * @author xiaojing
 * @author freeman
 */
@SpringBootTest(classes = NacosConfigEndpointTests.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.application.name=test-name",
		"spring.cloud.nacos.config.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.config.file-extension=properties",
		"spring.cloud.bootstrap.enabled=true" })
public class NacosConfigEndpointTests {

	@Autowired
	private NacosConfigProperties properties;

	@Autowired
	private NacosRefreshHistory refreshHistory;

	static {

		try {
			NacosConfigService mockedNacosConfigService = Mockito
					.mock(NacosConfigService.class);
			Mockito.when(mockedNacosConfigService.getServerStatus()).thenReturn("UP");
			ReflectionTestUtils.setField(NacosConfigManager.class, "service",
					mockedNacosConfigService);
		}
		catch (Exception ignore) {
			ignore.printStackTrace();
		}
	}
	@Test
	public void contextLoads() throws Exception {

		checkoutEndpoint();
		checkoutAcmHealthIndicator();

	}

	private void checkoutAcmHealthIndicator() {
		try {
			Builder builder = new Builder();

			NacosConfigHealthIndicator healthIndicator = new NacosConfigHealthIndicator(
					properties.configServiceInstance());
			healthIndicator.doHealthCheck(builder);

			Builder builder1 = new Builder();
			builder1.up();

			assertThat(builder.build()).isEqualTo(builder1.build());
		}
		catch (Exception ignore) {

		}

	}

	private void checkoutEndpoint() throws Exception {
		NacosConfigEndpoint endpoint = new NacosConfigEndpoint(properties,
				refreshHistory);
		Map<String, Object> map = endpoint.invoke();

		assertThat(properties).isEqualTo(map.get("NacosConfigProperties"));
		assertThat(refreshHistory.getRecords()).isEqualTo(map.get("RefreshHistory"));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ NacosConfigEndpointAutoConfiguration.class,
			NacosConfigAutoConfiguration.class, NacosConfigBootstrapConfiguration.class })
	public static class TestConfig {

	}

}
