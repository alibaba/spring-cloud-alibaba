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

package com.alibaba.cloud.nacos.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Map;

import com.alibaba.cloud.nacos.NacosNamingManager;
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

import com.alibaba.cloud.nacos.NacosDiscoveryAutoConfiguration;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientAutoConfiguration;
import com.alibaba.cloud.nacos.endpoint.NacosDiscoveryEndpoint;

/**
 * @author xiaojing
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NacosAutoServiceRegistrationTests.TestConfig.class, properties = {
		"spring.application.name=myTestService1",
		"spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.discovery.endpoint=test-endpoint",
		"spring.cloud.nacos.discovery.namespace=test-namespace",
		"spring.cloud.nacos.discovery.log-name=test-logName",
		"spring.cloud.nacos.discovery.weight=2",
		"spring.cloud.nacos.discovery.clusterName=test-cluster",
		"spring.cloud.nacos.discovery.namingLoadCacheAtStart=true",
		"spring.cloud.nacos.discovery.secure=true",
		"spring.cloud.nacos.discovery.accessKey=test-accessKey",
		"spring.cloud.nacos.discovery.secretKey=test-secretKey",
		"spring.cloud.nacos.discovery.heart-beat-interval=3",
		"spring.cloud.nacos.discovery.heart-beat-timeout=6",
		"spring.cloud.nacos.discovery.ip-delete-timeout=9", }, webEnvironment = RANDOM_PORT)
public class NacosAutoServiceRegistrationTests {

	@Autowired
	private NacosRegistration registration;

	@Autowired
	private NacosAutoServiceRegistration nacosAutoServiceRegistration;

	@LocalServerPort
	private int port;

	@Autowired
	private NacosDiscoveryProperties properties;

	@Autowired
	private NacosNamingManager nacosNamingManager;

	@Autowired
	private InetUtils inetUtils;

	@Test
	public void contextLoads() throws Exception {

		assertNotNull("NacosRegistration was not created", registration);
		assertNotNull("NacosDiscoveryProperties was not created", properties);
		assertNotNull("NacosAutoServiceRegistration was not created",
				nacosAutoServiceRegistration);

		checkoutNacosDiscoveryServerAddr();
		checkoutNacosDiscoveryEndpoint();
		checkoutNacosDiscoveryNamespace();
		checkoutNacosDiscoveryLogName();
		checkoutNacosDiscoveryWeight();
		checkoutNacosDiscoveryClusterName();
		checkoutNacosDiscoveryCache();
		checkoutNacosDiscoverySecure();
		checkoutNacosDiscoveryAccessKey();
		checkoutNacosDiscoverySecrectKey();
		checkoutNacosDiscoveryHeartBeatInterval();
		checkoutNacosDiscoveryHeartBeatTimeout();
		checkoutNacosDiscoveryIpDeleteTimeout();

		checkoutNacosDiscoveryServiceName();
		checkoutNacosDiscoveryServiceIP();
		checkoutNacosDiscoveryServicePort();

		checkAutoRegister();

		checkoutEndpoint();

	}

	private void checkAutoRegister() {
		assertTrue("Nacos Auto Registration was not start",
				nacosAutoServiceRegistration.isRunning());
	}

	private void checkoutNacosDiscoveryServerAddr() {
		assertEquals("NacosDiscoveryProperties server address was wrong",
				"127.0.0.1:8848", properties.getServerAddr());

	}

	private void checkoutNacosDiscoveryEndpoint() {
		assertEquals("NacosDiscoveryProperties endpoint was wrong", "test-endpoint",
				properties.getEndpoint());

	}

	private void checkoutNacosDiscoveryNamespace() {
		assertEquals("NacosDiscoveryProperties namespace was wrong", "test-namespace",
				properties.getNamespace());

	}

	private void checkoutNacosDiscoveryLogName() {
		assertEquals("NacosDiscoveryProperties logName was wrong", "test-logName",
				properties.getLogName());
	}

	private void checkoutNacosDiscoveryWeight() {
		assertEquals(2, properties.getWeight(), 0.00000001);
	}

	private void checkoutNacosDiscoveryClusterName() {
		assertEquals("NacosDiscoveryProperties cluster was wrong", "test-cluster",
				properties.getClusterName());
	}

	private void checkoutNacosDiscoveryCache() {
		assertEquals("NacosDiscoveryProperties naming load cache was wrong", "true",
				properties.getNamingLoadCacheAtStart());
	}

	private void checkoutNacosDiscoverySecure() {
		assertEquals("NacosDiscoveryProperties is secure was wrong", true,
				properties.isSecure());
		assertEquals("NacosDiscoveryProperties is secure was wrong", "true",
				properties.getMetadata().get("secure"));
	}

	private void checkoutNacosDiscoveryAccessKey() {
		assertEquals("NacosDiscoveryProperties is access key was wrong", "test-accessKey",
				properties.getAccessKey());
	}

	private void checkoutNacosDiscoverySecrectKey() {
		assertEquals("NacosDiscoveryProperties is secret key was wrong", "test-secretKey",
				properties.getSecretKey());
	}

	private void checkoutNacosDiscoveryHeartBeatInterval() {
		assertEquals("NacosDiscoveryProperties heart beat interval was wrong",
				Integer.valueOf(3), properties.getHeartBeatInterval());
	}

	private void checkoutNacosDiscoveryHeartBeatTimeout() {
		assertEquals("NacosDiscoveryProperties heart beat timeout was wrong",
				Integer.valueOf(6), properties.getHeartBeatTimeout());
	}

	private void checkoutNacosDiscoveryIpDeleteTimeout() {
		assertEquals("NacosDiscoveryProperties ip delete timeout was wrong",
				Integer.valueOf(9), properties.getIpDeleteTimeout());
	}

	private void checkoutNacosDiscoveryServiceName() {
		assertEquals("NacosDiscoveryProperties service name was wrong", "myTestService1",
				properties.getService());

	}

	private void checkoutNacosDiscoveryServiceIP() {
		assertEquals("NacosDiscoveryProperties service IP was wrong",
				inetUtils.findFirstNonLoopbackHostInfo().getIpAddress(),
				registration.getHost());

	}

	private void checkoutNacosDiscoveryServicePort() {
		assertEquals("NacosDiscoveryProperties service Port was wrong", port,
				registration.getPort());

	}

	private void checkoutEndpoint() throws Exception {
		NacosDiscoveryEndpoint nacosDiscoveryEndpoint = new NacosDiscoveryEndpoint(
				nacosNamingManager, properties);
		Map<String, Object> map = nacosDiscoveryEndpoint.nacosDiscovery();
		assertEquals(map.get("NacosDiscoveryProperties"), properties);
		assertEquals(map.get("subscribe").toString(),
				nacosNamingManager.getNamingService().getSubscribeServices().toString());
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			NacosDiscoveryClientAutoConfiguration.class,
			NacosDiscoveryAutoConfiguration.class })
	public static class TestConfig {
	}
}
