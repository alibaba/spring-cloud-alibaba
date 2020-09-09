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

package com.alibaba.cloud.nacos.registry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.alibaba.cloud.nacos.endpoint.NacosDiscoveryEndpoint;
import com.alibaba.nacos.api.NacosFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.MethodProxy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

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

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ NacosFactory.class })
@SpringBootTest(classes = NacosAutoServiceRegistrationTests.TestConfig.class,
		properties = { "spring.application.name=myTestService1",
				"spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848",
				"spring.cloud.nacos.discovery.endpoint=test-endpoint",
				"spring.cloud.nacos.discovery.namespace=test-namespace",
				"spring.cloud.nacos.discovery.log-name=test-logName",
				"spring.cloud.nacos.discovery.weight=2",
				"spring.cloud.nacos.discovery.clusterName=test-cluster",
				"spring.cloud.nacos.discovery.namingLoadCacheAtStart=true",
				"spring.cloud.nacos.discovery.secure=true",
				"spring.cloud.nacos.discovery.accessKey=test-accessKey",
				"spring.cloud.nacos.discovery.ip=8.8.8.8",
				"spring.cloud.nacos.discovery.secretKey=test-secretKey",
				"spring.cloud.nacos.discovery.heart-beat-interval=3",
				"spring.cloud.nacos.discovery.heart-beat-timeout=6",
				"spring.cloud.nacos.discovery.ip-delete-timeout=9" },
		webEnvironment = RANDOM_PORT)
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
	private NacosServiceManager nacosServiceManager;

	@Autowired
	private InetUtils inetUtils;

	static {
		try {
			Method method = PowerMockito.method(NacosFactory.class, "createNamingService",
					Properties.class);
			MethodProxy.proxy(method, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					return new MockNamingService();
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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

		checkAutoRegister();

		checkoutEndpoint();

	}

	private void checkAutoRegister() {
		assertThat(nacosAutoServiceRegistration.isRunning()).isEqualTo(Boolean.TRUE);
	}

	private void checkoutNacosDiscoveryServerAddr() {
		assertThat(properties.getServerAddr()).isEqualTo("127.0.0.1:8848");
	}

	private void checkoutNacosDiscoveryEndpoint() {
		assertThat(properties.getEndpoint()).isEqualTo("test-endpoint");
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
		assertThat(properties.getHeartBeatInterval()).isEqualTo(Integer.valueOf(3));
	}

	private void checkoutNacosDiscoveryHeartBeatTimeout() {
		assertThat(properties.getHeartBeatTimeout()).isEqualTo(Integer.valueOf(6));
	}

	private void checkoutNacosDiscoveryIpDeleteTimeout() {
		assertThat(properties.getIpDeleteTimeout()).isEqualTo(Integer.valueOf(9));
	}

	private void checkoutNacosDiscoveryServiceName() {
		assertThat(properties.getService()).isEqualTo("myTestService1");
	}

	private void checkoutNacosDiscoveryServiceIP() {
		assertThat(registration.getHost()).isEqualTo("8.8.8.8");
	}

	private void checkoutNacosDiscoveryServicePort() {
		assertThat(registration.getPort()).isEqualTo(port);
	}

	private void checkoutEndpoint() throws Exception {
		NacosDiscoveryEndpoint nacosDiscoveryEndpoint = new NacosDiscoveryEndpoint(
				nacosServiceManager, properties);
		Map<String, Object> map = nacosDiscoveryEndpoint.nacosDiscovery();

		assertThat(properties).isEqualTo(map.get("NacosDiscoveryProperties"));
		// assertThat(properties.namingServiceInstance().getSubscribeServices().toString())
		// .isEqualTo(map.get("subscribe").toString());
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			NacosDiscoveryClientConfiguration.class,
			NacosServiceRegistryAutoConfiguration.class })
	public static class TestConfig {

	}

}
