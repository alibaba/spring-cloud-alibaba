/*
 * Copyright 2026-2026 the original author or authors.
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

package com.alibaba.cloud.nacos;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.alibaba.cloud.nacos.event.NacosDiscoveryInfoChangedEvent;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration;
import com.alibaba.cloud.nacos.util.InetIPv6Utils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static com.alibaba.cloud.nacos.NacosDiscoveryPropertiesTests.TestConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author xuxiaowei
 */
@EnableDiscoveryClient(autoRegister = false)
@SpringBootTest(classes = TestConfig.class,
		properties = {
		"spring.application.name=app",
		"spring.cloud.nacos.discovery.server-addr=321.321.321.321:8848",
		"spring.cloud.nacos.server-addr=123.123.123.123:8848" })
class NacosDiscoveryPropertiesTests {

	private static final String KEY = UUID.randomUUID().toString();

	@Autowired
	private NacosDiscoveryProperties properties;

	@Test
	public void testGetServerAddr() {
		assertThat(properties.getServerAddr()).isEqualTo("321.321.321.321:8848");
	}

	@Test
	public void testNacosDiscoveryInfoChangedEvent() throws Exception {
		TestConfig.eventPublished = false;
		// modify some property
		String password = UUID.randomUUID().toString();
		String value = UUID.randomUUID().toString();
		properties.setPassword(password);
		properties.getMetadata().put(KEY, value);
		// trigger init
		properties.init();
		// check if event is published
		assertThat(TestConfig.eventPublished).isTrue();
		assertThat(TestConfig.password).isEqualTo(password);
		assertThat(TestConfig.metadataValue).isEqualTo(value);
	}

	@Test
	public void toStringShouldRenderDiscoveryFieldsOnce() {
		NacosDiscoveryProperties properties = new NacosDiscoveryProperties();
		properties.setIpType("IPv6");
		properties.setIpDeleteTimeout(30000);
		properties.setGracefulShutdownWaitTime(5000);

		String value = properties.toString();

		assertThat(value).contains(", ipType='IPv6'");
		assertThat(value).contains(", gracefulShutdownWaitTime=5000");
		assertThat(value).contains(", failFast=true");
		assertThat(value).containsOnlyOnce("ipDeleteTimeout=");
		assertThat(value).doesNotContain("}, ipDeleteTimeout=");
		assertThat(value).endsWith("}");
	}

	@Test
	public void equalsAndHashCodeShouldIncludeIpType() {
		NacosDiscoveryProperties left = new NacosDiscoveryProperties();
		NacosDiscoveryProperties right = new NacosDiscoveryProperties();

		left.setIpType("IPv4");
		right.setIpType("IPv6");

		assertThat(left).isNotEqualTo(right);
		assertThat(left.hashCode()).isNotEqualTo(right.hashCode());
	}

	@Test
	public void equalsAndHashCodeShouldIncludeGracefulShutdownWaitTime() {
		NacosDiscoveryProperties left = new NacosDiscoveryProperties();
		NacosDiscoveryProperties right = new NacosDiscoveryProperties();

		left.setGracefulShutdownWaitTime(5000);
		right.setGracefulShutdownWaitTime(10000);

		assertThat(left).isNotEqualTo(right);
		assertThat(left.hashCode()).isNotEqualTo(right.hashCode());
	}

	@Test
	public void initShouldNotPutNullIpv6IntoMetadata() throws Exception {
		NacosDiscoveryProperties properties = new NacosDiscoveryProperties();
		properties.setServerAddr("127.0.0.1:8848");
		properties.setInetUtils(inetUtils("192.168.1.10"));
		ReflectionTestUtils.setField(properties, "inetIPv6Utils", noIpv6Utils());
		ReflectionTestUtils.setField(properties, "environment", mock(Environment.class));
		ReflectionTestUtils.setField(properties, "nacosServiceManager",
				mock(NacosServiceManager.class));
		ReflectionTestUtils.setField(properties, "applicationEventPublisher",
				mock(ApplicationEventPublisher.class));

		properties.init();

		assertThat(properties.getMetadata()).doesNotContainKey("IPv6");
	}

	@Test
	public void initShouldSkipLoopbackAddressFromNetworkInterface() throws Exception {
		NacosDiscoveryProperties properties = new NacosDiscoveryProperties();
		properties.setServerAddr("127.0.0.1:8848");
		properties.setNetworkInterface("eth-test");
		ReflectionTestUtils.setField(properties, "environment", mock(Environment.class));
		ReflectionTestUtils.setField(properties, "nacosServiceManager",
				mock(NacosServiceManager.class));
		ReflectionTestUtils.setField(properties, "applicationEventPublisher",
				mock(ApplicationEventPublisher.class));

		NetworkInterface networkInterface = mock(NetworkInterface.class);
		when(networkInterface.getInetAddresses())
				.thenReturn(Collections.enumeration(List.of(
						InetAddress.getByName("127.0.0.1"),
						InetAddress.getByName("192.168.1.10"))));

		try (MockedStatic<NetworkInterface> mockedNetworkInterface = mockStatic(
				NetworkInterface.class)) {
			mockedNetworkInterface.when(() -> NetworkInterface.getByName("eth-test"))
					.thenReturn(networkInterface);

			properties.init();
		}

		assertThat(properties.getIp()).isEqualTo("192.168.1.10");
	}

	private static InetUtils inetUtils(String ipAddress) {
		InetUtils inetUtils = mock(InetUtils.class);
		InetUtils.HostInfo hostInfo = new InetUtils.HostInfo();
		hostInfo.setIpAddress(ipAddress);
		when(inetUtils.findFirstNonLoopbackHostInfo()).thenReturn(hostInfo);
		return inetUtils;
	}

	private static InetIPv6Utils noIpv6Utils() {
		InetIPv6Utils inetIPv6Utils = mock(InetIPv6Utils.class);
		when(inetIPv6Utils.findIPv6Address()).thenReturn(null);
		return inetIPv6Utils;
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			NacosDiscoveryClientConfiguration.class,
			NacosServiceRegistryAutoConfiguration.class })
	public static class TestConfig {

		/**
		 * eventPublished.
		 */
		public static boolean eventPublished = false;

		/**
		 * password.
		 */
		public static String password;

		/**
		 * metadataValue.
		 */
		public static String metadataValue;

		@EventListener
		public void onEvent(NacosDiscoveryInfoChangedEvent event) {
			eventPublished = true;
			password = event.getSource().getPassword();
			metadataValue = event.getSource().getMetadata().get(KEY);
		}

	}

}
