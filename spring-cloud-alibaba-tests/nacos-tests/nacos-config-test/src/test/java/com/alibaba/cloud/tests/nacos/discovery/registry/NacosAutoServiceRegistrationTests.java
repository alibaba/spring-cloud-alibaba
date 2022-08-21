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

import java.util.Map;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.alibaba.cloud.nacos.endpoint.NacosDiscoveryEndpoint;
import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration;
import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringCloudAlibaba(composeFiles = "docker/nacos-compose-test.yml", serviceName = "nacos-standalone")
@TestExtend(time = 4 * TIME_OUT)
@SpringBootTest(classes = NacosAutoServiceRegistrationTests.TestConfig.class, properties = {
		"spring.application.name=myTestService1",
		"spring.cloud.nacos.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.discovery.endpoint=127.0.0.1",
		"spring.cloud.nacos.discovery.namespace=test-namespace",
		"spring.cloud.nacos.discovery.log-name=test-logName",
		"spring.cloud.nacos.discovery.weight=2",
		"spring.cloud.nacos.discovery.clusterName=test-cluster",
		"spring.cloud.nacos.discovery.namingLoadCacheAtStart=true",
		"spring.cloud.nacos.discovery.secure=true",
		"spring.cloud.nacos.discovery.accessKey=test-accessKey",
		"spring.cloud.nacos.discovery.ip=127.0.0.1",
		"spring.cloud.nacos.discovery.secretKey=test-secretKey",
		"spring.cloud.nacos.discovery.heart-beat-interval=3000",
		"spring.cloud.nacos.discovery.heart-beat-timeout=6000",
		"spring.cloud.nacos.discovery.ip-delete-timeout=9000",
		"spring.cloud.nacos.discovery.port=8888",
		"spring.cloud.nacos.discovery.registerEnabled=true",
		"spring.cloud.nacos.username=nacos", "spring.cloud.nacos.password=nacos",
		"management.server.port=8888",
		"management.server.servlet.context-path=/test-context-path" }, webEnvironment = NONE)
public class NacosAutoServiceRegistrationTests {

	private static final String serviceName = "service-test";

	@Autowired
	private NacosRegistration registration;

	@Autowired
	private NacosAutoServiceRegistration nacosAutoServiceRegistration;

	@Autowired
	private NacosDiscoveryProperties properties;

	@Autowired
	private NacosServiceManager nacosServiceManager;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Mock
	private MockManagementServer mockManagementServer;

	@BeforeAll
	public static void setUp() throws NacosException {
		NamingFactory.createNamingService("127.0.0.1:8848");
	}

	@BeforeEach
	public void prepare() {
	}

	@Test
	public void contextLoads() throws Exception {
		assertThat(registration).isNotNull();
		assertThat(properties).isNotNull();
		assertThat(nacosAutoServiceRegistration).isNotNull();

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

		checkoutEndpoint();

	}

	private void checkoutNacosDiscoveryServerAddr() {
		assertThat(properties.getServerAddr()).isEqualTo("127.0.0.1:8848");
	}

	private void checkoutNacosDiscoveryEndpoint() {
		assertThat(properties.getEndpoint()).isEqualTo("127.0.0.1");
	}

	private void checkoutNacosDiscoveryNamespace() {
		assertThat(properties.getNamespace()).isEqualTo("test-namespace");
	}

	private void checkoutNacosDiscoveryLogName() {
		assertThat(properties.getLogName()).isEqualTo("test-logName");
	}

	private void checkoutNacosDiscoveryWeight() {
		assertThat(properties.getWeight()).isEqualTo(2);
	}

	private void checkoutNacosDiscoveryClusterName() {
		assertThat(properties.getClusterName()).isEqualTo("test-cluster");
	}

	private void checkoutNacosDiscoveryCache() {
		assertThat(properties.getNamingLoadCacheAtStart()).isEqualTo("true");
	}

	private void checkoutNacosDiscoverySecure() {
		assertThat(properties.isSecure()).isEqualTo(true);
		assertThat(properties.getMetadata().get("secure")).isEqualTo("true");
	}

	private void checkoutNacosDiscoveryAccessKey() {
		assertThat(properties.getAccessKey()).isEqualTo("test-accessKey");
	}

	private void checkoutNacosDiscoverySecrectKey() {
		assertThat(properties.getSecretKey()).isEqualTo("test-secretKey");
	}

	private void checkoutNacosDiscoveryHeartBeatInterval() {
		assertThat(properties.getHeartBeatInterval()).isEqualTo(Integer.valueOf(3000));
	}

	private void checkoutNacosDiscoveryHeartBeatTimeout() {
		assertThat(properties.getHeartBeatTimeout()).isEqualTo(Integer.valueOf(6000));
	}

	private void checkoutNacosDiscoveryIpDeleteTimeout() {
		assertThat(properties.getIpDeleteTimeout()).isEqualTo(Integer.valueOf(9000));
	}

	private void checkoutNacosDiscoveryServiceName() {
		assertThat(properties.getService()).isEqualTo("myTestService1");
	}

	private void checkoutNacosDiscoveryServiceIP() {
		assertThat(registration.getHost()).isEqualTo("127.0.0.1");
	}

	private void checkoutNacosDiscoveryServicePort() {
		assertThat(registration.getPort()).isEqualTo(8888);
	}

	private void checkoutEndpoint() throws Exception {
		NacosDiscoveryEndpoint nacosDiscoveryEndpoint = new NacosDiscoveryEndpoint(
				nacosServiceManager, properties);
		Map<String, Object> map = nacosDiscoveryEndpoint.nacosDiscovery();

		assertThat(properties).isEqualTo(map.get("NacosDiscoveryProperties"));
		assertThat(properties.namingServiceInstance().getSubscribeServices().toString())
				.isEqualTo(map.get("subscribe").toString());
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			NacosDiscoveryClientConfiguration.class,
			NacosServiceRegistryAutoConfiguration.class })
	public static class TestConfig {

	}

	@Endpoint(id = "127.0.0.1")
	public static class MockManagementServer {
		private final ManagementServerProperties sentinelProperties;

		public MockManagementServer(
				ManagementServerProperties managementServerProperties) {
			this.sentinelProperties = managementServerProperties;
		}
	}

}
