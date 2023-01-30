/*
 * Copyright 2013-2023 the original author or authors.
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

import java.util.Arrays;
import java.util.List;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.cloud.nacos.discovery.ServiceCache;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.test.util.ReflectionTestUtils;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * @author xiaojing
 * @author echooymxq
 * @author freeman
 */
@ExtendWith(MockitoExtension.class)
public class NacosDiscoveryClientTests {

	@Mock
	private NacosServiceDiscovery serviceDiscovery;

	@Mock
	private NacosServiceInstance serviceInstance;

	@InjectMocks
	private NacosDiscoveryClient client;

	@Test
	public void testGetInstances() throws Exception {

		when(serviceDiscovery.getInstances("service-1"))
				.thenReturn(singletonList(serviceInstance));

		List<ServiceInstance> serviceInstances = client.getInstances("service-1");

		assertThat(serviceInstances).isNotEmpty();

	}

	@Test
	public void testGetServices() throws Exception {

		when(serviceDiscovery.getServices()).thenReturn(singletonList("service-1"));

		List<String> services = client.getServices();

		assertThat(services).contains("service-1").size().isEqualTo(1);

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
		assertThat(ServiceCache.getInstances("a")).isEqualTo(singletonList(serviceInstance));
	}

}
