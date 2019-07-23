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

package com.alibaba.alicloud.ans.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.alibaba.alicloud.ans.AnsAutoConfiguration;
import com.alibaba.alicloud.ans.AnsDiscoveryClientAutoConfiguration;
import com.alibaba.alicloud.ans.endpoint.AnsEndpoint;
import com.alibaba.alicloud.context.ans.AnsProperties;
import com.alibaba.ans.core.NamingService;
import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;

/**
 * @author xiaojing
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnsAutoServiceRegistrationTests.TestConfig.class, properties = {
		"spring.application.name=myTestService1",
		"spring.cloud.alicloud.ans.server-list=127.0.0.1",
		"spring.cloud.alicloud.ans.server-port=8080",
		"spring.cloud.alicloud.ans.secure=true",
		"spring.cloud.alicloud.ans.endpoint=test-endpoint" }, webEnvironment = RANDOM_PORT)
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

		assertNotNull("AnsRegistration was not created", registration);
		assertNotNull("AnsProperties was not created", properties);
		assertNotNull("AnsAutoServiceRegistration was not created",
				ansAutoServiceRegistration);

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
		assertTrue("Ans Auto Registration was not start",
				ansAutoServiceRegistration.isRunning());
	}

	private void checkoutAnsDiscoveryServerList() {
		assertEquals("AnsDiscoveryProperties server list was wrong", "127.0.0.1",
				properties.getServerList());
	}

	private void checkoutAnsDiscoveryServerPort() {
		assertEquals("AnsDiscoveryProperties server port was wrong", "8080",
				properties.getServerPort());
	}

	private void checkoutAnsDiscoveryServiceName() {
		assertEquals("AnsDiscoveryProperties service name was wrong", "myTestService1",
				properties.getClientDomains());
	}

	private void checkoutAnsDiscoveryServiceIP() {
		assertEquals("AnsDiscoveryProperties service IP was wrong",
				inetUtils.findFirstNonLoopbackHostInfo().getIpAddress(),
				registration.getHost());
	}

	private void checkoutAnsDiscoveryServicePort() {
		assertEquals("AnsDiscoveryProperties service Port was wrong", port,
				registration.getPort());
	}

	private void checkoutAnsDiscoverySecure() {
		assertTrue("AnsDiscoveryProperties secure should be true", properties.isSecure());

	}

	private void checkoutEndpoint() throws Exception {
		AnsEndpoint ansEndpoint = new AnsEndpoint(properties);
		Map<String, Object> map = ansEndpoint.invoke();
		assertEquals(map.get("ansProperties"), properties);

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

		assertEquals(map.get("subscribes"), subscribes);
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			AnsDiscoveryClientAutoConfiguration.class, AnsAutoConfiguration.class })
	public static class TestConfig {
	}
}
