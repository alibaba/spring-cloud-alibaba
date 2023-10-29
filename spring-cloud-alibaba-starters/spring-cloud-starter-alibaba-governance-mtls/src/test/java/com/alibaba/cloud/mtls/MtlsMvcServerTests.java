/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.mtls;

import com.alibaba.cloud.commons.governance.ControlPlaneInitedBean;
import com.alibaba.cloud.governance.istio.XdsAutoConfiguration;
import com.alibaba.cloud.governance.istio.sds.AbstractCertManager;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "spring.main.web-application-type=servlet",
				"spring.cloud.nacos.discovery.enabled=false" },
		classes = { MtlsClientTests.TestConfig.class })
public class MtlsMvcServerTests {

	@Value("${local.server.port}")
	private int port;

	private CloseableHttpClient client = NoVerifyHttpClientFactory.getClient();

	@Test
	public void testStatusCode() throws Exception {
		boolean flag = false;
		CloseableHttpResponse response = client
				.execute(new HttpGet("http://127.0.0.1:" + port + "/echo"));
		Assert.assertEquals(400, response.getStatusLine().getStatusCode());
		response = client.execute(new HttpGet("https://127.0.0.1:" + port + "/echo"));
		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
	}

	@Configuration
	@EnableAutoConfiguration(exclude = XdsAutoConfiguration.class)
	@ImportAutoConfiguration(MtlsAutoConfiguration.class)
	public static class TestConfig {

		@Bean
		public AbstractCertManager mockCertManager() {
			return new MockCertManager();
		}

		@Bean
		public ControlPlaneInitedBean mockInitBean() {
			return new ControlPlaneInitedBean();
		}

	}

}
