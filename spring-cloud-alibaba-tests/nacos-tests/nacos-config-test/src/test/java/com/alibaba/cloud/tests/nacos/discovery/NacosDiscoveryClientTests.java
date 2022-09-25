/*
 * Copyright 2013-2022 the original author or authors.
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

package com.alibaba.cloud.tests.nacos.discovery;

import java.util.Arrays;
import java.util.List;

import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.cloud.nacos.discovery.ServiceCache;
import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.test.util.ReflectionTestUtils;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringCloudAlibaba(composeFiles = "docker/nacos-compose-test.yml", serviceName = "nacos-standalone")
@TestExtend(time = 4 * TIME_OUT)
@SpringBootTest(classes = NacosDiscoveryPropertiesServerAddressBothLevelTests.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.application.name=app",
		"spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.server-addr=127.0.0.1:8848" })
public class NacosDiscoveryClientTests {

	private static final String serviceName = "test-service";
	@Mock
	private NacosServiceDiscovery serviceDiscovery;
	@Mock
	private NacosServiceInstance serviceInstance;
	@InjectMocks
	private NacosDiscoveryClient client;

	@Test
	public void testGetInstances() throws Exception {

		when(serviceDiscovery.getInstances(serviceName))
				.thenReturn(singletonList(serviceInstance));

		List<ServiceInstance> serviceInstances = client.getInstances(serviceName);

		assertThat(serviceInstances).isNotEmpty();

	}

	@Test
	public void testGetServices() throws Exception {

		when(serviceDiscovery.getServices()).thenReturn(singletonList(serviceName));

		List<String> services = client.getServices();

		assertThat(services).contains(serviceName).size().isEqualTo(1);

	}

	@Test
	public void testGetInstancesFailureToleranceEnabled() throws NacosException {
		ServiceCache.setInstances("a", singletonList(serviceInstance));

		when(serviceDiscovery.getInstances("a")).thenThrow(new NacosException());
		ReflectionTestUtils.setField(client, "failureToleranceEnabled", true);

		List<ServiceInstance> instances = this.client.getInstances("a");

		assertThat(instances).isEqualTo(singletonList(serviceInstance));
	}

	@Test
	public void testGetInstancesFailureToleranceDisabled() throws NacosException {
		ServiceCache.setInstances("a", singletonList(serviceInstance));

		when(serviceDiscovery.getInstances("a")).thenThrow(new NacosException());
		ReflectionTestUtils.setField(client, "failureToleranceEnabled", false);

		assertThatThrownBy(() -> this.client.getInstances("a"));
	}

	@Test
	public void testFailureToleranceEnabled() throws NacosException {
		ServiceCache.setServiceIds(Arrays.asList("a", "b"));

		when(serviceDiscovery.getServices()).thenThrow(new NacosException());
		ReflectionTestUtils.setField(client, "failureToleranceEnabled", true);

		List<String> services = this.client.getServices();

		assertThat(services).isEqualTo(Arrays.asList("a", "b"));
	}

	@Test
	public void testFailureToleranceDisabled() throws NacosException {
		ServiceCache.setServiceIds(Arrays.asList("a", "b"));

		when(serviceDiscovery.getServices()).thenThrow(new NacosException());
		ReflectionTestUtils.setField(client, "failureToleranceEnabled", false);

		List<String> services = this.client.getServices();

		assertThat(services).isEqualTo(emptyList());
	}

	@Test
	public void testCacheIsOK() throws NacosException {
		when(serviceDiscovery.getInstances("a"))
				.thenReturn(singletonList(serviceInstance));
		this.client.getInstances("a");
		assertThat(ServiceCache.getInstances("a"))
				.isEqualTo(singletonList(serviceInstance));
	}

}
