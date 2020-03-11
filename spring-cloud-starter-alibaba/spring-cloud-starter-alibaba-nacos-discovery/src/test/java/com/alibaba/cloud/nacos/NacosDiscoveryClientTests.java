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

package com.alibaba.cloud.nacos;

import java.util.List;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.cloud.client.ServiceInstance;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author xiaojing
 * @author echooymxq
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

}
