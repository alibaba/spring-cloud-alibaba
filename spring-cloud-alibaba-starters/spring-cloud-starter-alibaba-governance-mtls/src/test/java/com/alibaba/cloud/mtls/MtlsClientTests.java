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
import com.alibaba.cloud.mtls.client.MtlsClientSSLContext;
import com.alibaba.cloud.mtls.server.netty.MtlsNettyServerCustomizer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.SslInfo;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "spring.main.web-application-type=reactive",
				"spring.cloud.nacos.discovery.enabled=false" },
		classes = { MtlsClientTests.TestConfig.class })
public class MtlsClientTests {

	@Value("${local.server.port}")
	private int port;

	private static CloseableHttpClient client;

	private static CloseableHttpClient noCertClient;

	@Autowired
	private MtlsClientSSLContext context;

	public void init() {
		if (client == null) {
			client = NoVerifyHttpClientFactory.getClient(context.getSslContext());
		}
		if (noCertClient == null) {
			noCertClient = NoVerifyHttpClientFactory.getClient();
		}
	}

	@Test
	public void testStatusCode() throws Exception {
		init();
		CloseableHttpResponse response = client
				.execute(new HttpGet("https://127.0.0.1:" + port + "/echo"));
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
		response = noCertClient
				.execute(new HttpGet("https://127.0.0.1:" + port + "/echo"));
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 500);
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

		@Bean
		public NettyReactiveWebServerFactory nettyFactory(
				MtlsNettyServerCustomizer customizer) {
			NettyReactiveWebServerFactory nettyReactiveWebServerFactory = new NettyReactiveWebServerFactory();
			nettyReactiveWebServerFactory.addServerCustomizers(customizer);
			return nettyReactiveWebServerFactory;
		}

		@RestController
		public static class EchoController {

			@RequestMapping("/echo")
			public String echo(ServerWebExchange exchange) {
				if (exchange.getRequest().getSslInfo() != null) {
					SslInfo sslInfo = exchange.getRequest().getSslInfo();
					if (sslInfo.getPeerCertificates() != null) {
						return "success";
					}
				}
				throw new RuntimeException("No client certificate");
			}

		}

	}

}
