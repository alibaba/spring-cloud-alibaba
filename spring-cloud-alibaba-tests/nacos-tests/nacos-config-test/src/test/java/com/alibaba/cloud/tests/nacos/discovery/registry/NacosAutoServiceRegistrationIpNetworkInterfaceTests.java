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

package com.alibaba.cloud.tests.nacos.discovery.registry;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration;
import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringCloudAlibaba(composeFiles = "docker/nacos-compose-test.yml", serviceName = "nacos-standalone")
@TestExtend(time = 4 * TIME_OUT)
@SpringBootTest(classes = NacosAutoServiceRegistrationIpNetworkInterfaceTests.TestConfig.class, properties = {
		"spring.application.name=myTestService1",
		"spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848" }, webEnvironment = NONE)
public class NacosAutoServiceRegistrationIpNetworkInterfaceTests {

	private static MockedStatic<NacosFactory> nacosFactoryMockedStatic;
	@Autowired
	private NacosRegistration registration;
	@Autowired
	private NacosAutoServiceRegistration nacosAutoServiceRegistration;
	@Autowired
	private NacosDiscoveryProperties properties;
	@Autowired
	private InetUtils inetUtils;

	@BeforeAll
	public static void setUp() throws NacosException {
		NamingFactory.createNamingService("127.0.0.1:8848");
	}

	@AfterAll
	public static void finished() {
		if (nacosFactoryMockedStatic != null) {
			nacosFactoryMockedStatic.close();
		}
	}

	@BeforeEach
	public void prepare() {
	}

	@Test
	public void contextLoads() throws Exception {
		assertThat(registration).isNotNull();
		assertThat(properties).isNotNull();
		assertThat(nacosAutoServiceRegistration).isNotNull();

		checkoutNacosDiscoveryServiceIP();
	}

	private void checkoutNacosDiscoveryServiceIP() {
		assertThat(registration.getHost())
				.isEqualTo(getIPFromNetworkInterface(TestConfig.netWorkInterfaceName));
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
			NacosDiscoveryClientConfiguration.class,
			NacosServiceRegistryAutoConfiguration.class })
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
									"spring.cloud.nacos.discovery.network-interface",
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
