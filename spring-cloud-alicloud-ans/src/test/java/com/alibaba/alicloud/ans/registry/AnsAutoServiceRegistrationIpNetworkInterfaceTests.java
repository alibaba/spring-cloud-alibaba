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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.alicloud.ans.AnsAutoConfiguration;
import com.alibaba.alicloud.ans.AnsDiscoveryClientAutoConfiguration;
import com.alibaba.alicloud.context.ans.AnsProperties;

/**
 * @author xiaojing
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnsAutoServiceRegistrationIpNetworkInterfaceTests.TestConfig.class, properties = {
		"spring.application.name=myTestService1",
		"spring.cloud.alicloud.ans.server-list=127.0.0.1",
		"spring.cloud.alicloud.ans.server-port=8080" }, webEnvironment = RANDOM_PORT)
public class AnsAutoServiceRegistrationIpNetworkInterfaceTests {

	@Autowired
	private AnsRegistration registration;

	@Autowired
	private AnsAutoServiceRegistration ansAutoServiceRegistration;

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

		checkoutAnsDiscoveryServiceIP();

	}

	private void checkoutAnsDiscoveryServiceIP() {
		assertEquals("AnsProperties service IP was wrong",
				getIPFromNetworkInterface(TestConfig.netWorkInterfaceName),
				registration.getHost());

	}

	private String getIPFromNetworkInterface(String networkInterface) {

		if (!TestConfig.hasValidNetworkInterface) {
			return inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
		}

		try {
			NetworkInterface netInterface = NetworkInterface.getByName(networkInterface);

			Enumeration<InetAddress> inetAddress = netInterface.getInetAddresses();
			while (inetAddress.hasMoreElements()) {
				InetAddress currentAddress = inetAddress.nextElement();
				if (currentAddress instanceof Inet4Address
						&& !currentAddress.isLoopbackAddress()) {
					return currentAddress.getHostAddress();
				}
			}
			return networkInterface;
		}
		catch (Exception e) {
			return networkInterface;
		}
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			AnsDiscoveryClientAutoConfiguration.class, AnsAutoConfiguration.class })
	public static class TestConfig {

		static boolean hasValidNetworkInterface = false;
		static String netWorkInterfaceName;

		static {

			try {
				Enumeration<NetworkInterface> enumeration = NetworkInterface
						.getNetworkInterfaces();
				while (enumeration.hasMoreElements() && !hasValidNetworkInterface) {
					NetworkInterface networkInterface = enumeration.nextElement();
					Enumeration<InetAddress> inetAddress = networkInterface
							.getInetAddresses();
					while (inetAddress.hasMoreElements()) {
						InetAddress currentAddress = inetAddress.nextElement();
						if (currentAddress instanceof Inet4Address
								&& !currentAddress.isLoopbackAddress()) {
							hasValidNetworkInterface = true;
							netWorkInterfaceName = networkInterface.getName();
							System.setProperty(
									"spring.cloud.alicloud.ans.client-interface-name",
									networkInterface.getName());
							break;
						}
					}
				}

			}
			catch (Exception e) {

			}
		}
	}

}
