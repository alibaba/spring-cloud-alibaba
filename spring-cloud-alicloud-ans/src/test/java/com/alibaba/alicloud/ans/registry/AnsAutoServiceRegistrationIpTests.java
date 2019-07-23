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
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.alicloud.ans.AnsAutoConfiguration;
import com.alibaba.alicloud.ans.AnsDiscoveryClientAutoConfiguration;
import com.alibaba.alicloud.context.ans.AnsProperties;

/**
 * @author xiaojing
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnsAutoServiceRegistrationIpTests.TestConfig.class, properties = {
		"spring.application.name=myTestService1",
		"spring.cloud.alicloud.ans.client-domains=myTestService2",
		"spring.cloud.alicloud.ans.server-list=127.0.0.1",
		"spring.cloud.alicloud.ans.client-weight=2",
		"spring.cloud.alicloud.ans.server-port=8080",
		"spring.cloud.alicloud.ans.client-ip=123.123.123.123" }, webEnvironment = RANDOM_PORT)
public class AnsAutoServiceRegistrationIpTests {

	@Autowired
	private AnsRegistration registration;

	@Autowired
	private AnsAutoServiceRegistration ansAutoServiceRegistration;

	@Autowired
	private AnsProperties properties;

	@Test
	public void contextLoads() throws Exception {

		assertNotNull("AnsRegistration was not created", registration);
		assertNotNull("AnsProperties was not created", properties);
		assertNotNull("AnsAutoServiceRegistration was not created",
				ansAutoServiceRegistration);

		checkoutAnsDiscoveryServiceIP();
		checkoutAnsDiscoveryServiceName();
		checkoutAnsDiscoveryWeight();
	}

	private void checkoutAnsDiscoveryServiceIP() {
		assertEquals("AnsProperties service IP was wrong", "123.123.123.123",
				registration.getHost());
	}

	private void checkoutAnsDiscoveryServiceName() {
		assertEquals("AnsDiscoveryProperties service name was wrong", "myTestService2",
				properties.getClientDomains());
	}

	private void checkoutAnsDiscoveryWeight() {
		assertEquals(2L, properties.getClientWeight(), 0);
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			AnsDiscoveryClientAutoConfiguration.class, AnsAutoConfiguration.class })
	public static class TestConfig {
	}
}
