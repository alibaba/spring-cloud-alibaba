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

package com.alibaba.alicloud.ans.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.alicloud.ans.AnsAutoConfiguration;
import com.alibaba.alicloud.ans.AnsDiscoveryClientAutoConfiguration;
import com.alibaba.alicloud.ans.endpoint.AnsEndpoint;
import com.alibaba.alicloud.context.ans.AnsProperties;
import com.alibaba.ans.core.NamingService;
import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author xiaojing
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnsAutoServiceRegistrationTests.TestConfig.class,
		properties = { "spring.application.name=myTestService1",
				"spring.cloud.alicloud.ans.server-list=127.0.0.1",
				"spring.cloud.alicloud.ans.server-port=8080",
				"spring.cloud.alicloud.ans.secure=true",
				"spring.cloud.alicloud.ans.endpoint=test-endpoint" },
		webEnvironment = RANDOM_PORT)
public class AnsAutoServiceRegistrationTests {

	@Autowired
	private AnsRegistration registration;

	@Autowired
	private AnsAutoServiceRegistration ansAutoServiceRegistration;

	@LocalServerPort
	private int port;

	@Autowired
	private AnsProperties properties;

	@Autowired
	private InetUtils inetUtils;

	@Test
	public void contextLoads() throws Exception {

		assertThat(registration).isNotNull();
		assertThat(properties).isNotNull();
		assertThat(ansAutoServiceRegistration).isNotNull();

		checkoutAnsDiscoveryServerList();
		checkoutAnsDiscoveryServerPort();

		checkoutAnsDiscoveryServiceName();
		checkoutAnsDiscoveryServiceIP();
		checkoutAnsDiscoveryServicePort();
		checkoutAnsDiscoverySecure();

		checkAutoRegister();

		checkoutEndpoint();

	}

	private void checkAutoRegister() {
		assertThat(ansAutoServiceRegistration.isRunning()).isEqualTo(Boolean.TRUE);
	}

	private void checkoutAnsDiscoveryServerList() {
		assertThat(properties.getServerList()).isEqualTo("127.0.0.1");
	}

	private void checkoutAnsDiscoveryServerPort() {
		assertThat(properties.getServerPort()).isEqualTo("8080");
	}

	private void checkoutAnsDiscoveryServiceName() {
		assertThat(properties.getClientDomains()).isEqualTo("myTestService1");
	}

	private void checkoutAnsDiscoveryServiceIP() {
		assertThat(registration.getHost())
				.isEqualTo(inetUtils.findFirstNonLoopbackHostInfo().getIpAddress());
	}

	private void checkoutAnsDiscoveryServicePort() {
		assertThat(registration.getPort()).isEqualTo(port);
	}

	private void checkoutAnsDiscoverySecure() {
		assertThat(properties.isSecure()).isEqualTo(Boolean.TRUE);

	}

	private void checkoutEndpoint() throws Exception {
		AnsEndpoint ansEndpoint = new AnsEndpoint(properties);
		Map<String, Object> map = ansEndpoint.invoke();
		assertThat(properties).isEqualTo(map.get("ansProperties"));

		Map<String, Object> subscribes = new HashMap<>();
		Set<String> subscribeServices = NamingService.getDomsSubscribed();
		for (String service : subscribeServices) {
			try {
				List<Host> hosts = NamingService.getHosts(service);
				subscribes.put(service, hosts);
			}
			catch (Exception ignoreException) {

			}
		}

		assertThat(subscribes).isEqualTo(map.get("subscribes"));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			AnsDiscoveryClientAutoConfiguration.class, AnsAutoConfiguration.class })
	public static class TestConfig {

	}

}
