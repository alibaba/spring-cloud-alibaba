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

package org.springframework.cloud.alicloud.ans.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.alicloud.ans.registry.AnsRegistration.MANAGEMENT_CONTEXT_PATH;
import static org.springframework.cloud.alicloud.ans.registry.AnsRegistration.MANAGEMENT_PORT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.alicloud.ans.AnsAutoConfiguration;
import org.springframework.cloud.alicloud.ans.AnsDiscoveryClientAutoConfiguration;
import org.springframework.cloud.alicloud.context.ans.AnsProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaojing
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnsAutoServiceRegistrationManagementPortTests.TestConfig.class, properties = {
		"spring.application.name=myTestService1", "management.server.port=8888",
		"management.server.servlet.context-path=/test-context-path",
		"spring.cloud.alicloud.ans.server-list=127.0.0.1",
		"spring.cloud.alicloud.ans.server-port=8080" }, webEnvironment = RANDOM_PORT)
public class AnsAutoServiceRegistrationManagementPortTests {

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

		checkoutNacosDiscoveryManagementData();

	}

	private void checkoutNacosDiscoveryManagementData() {
		assertEquals("AnsProperties management port was wrong", "8888",
				properties.getClientMetadata().get(MANAGEMENT_PORT));

		assertEquals("AnsProperties management context path was wrong",
				"/test-context-path",
				properties.getClientMetadata().get(MANAGEMENT_CONTEXT_PATH));

	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			AnsDiscoveryClientAutoConfiguration.class, AnsAutoConfiguration.class })
	public static class TestConfig {
	}
}
