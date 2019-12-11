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

package com.alibaba.cloud.nacos.ribbon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.test.NacosMockTest;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author xiaojing
 */

public class NacosServerListTests {

	@Test
	@SuppressWarnings("unchecked")
	public void testEmptyInstancesReturnsEmptyList() throws Exception {
		NacosDiscoveryProperties nacosDiscoveryProperties = mock(
				NacosDiscoveryProperties.class);

		NamingService namingService = mock(NamingService.class);

		when(nacosDiscoveryProperties.namingServiceInstance()).thenReturn(namingService);
		when(namingService.selectInstances(anyString(), eq("DEFAULT"), eq(true)))
				.thenReturn(null);

		NacosServerList serverList = new NacosServerList(nacosDiscoveryProperties);
		List<NacosServer> servers = serverList.getInitialListOfServers();
		assertThat(servers).isEmpty();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetServers() throws Exception {

		ArrayList<Instance> instances = new ArrayList<>();
		instances.add(NacosMockTest.serviceInstance("test-service", false,
				Collections.emptyMap()));

		NacosDiscoveryProperties nacosDiscoveryProperties = mock(
				NacosDiscoveryProperties.class);

		NamingService namingService = mock(NamingService.class);

		when(nacosDiscoveryProperties.namingServiceInstance()).thenReturn(namingService);
		when(nacosDiscoveryProperties.getGroup()).thenReturn("DEFAULT");
		when(nacosDiscoveryProperties.getGroup()).thenReturn("DEFAULT");
		when(namingService.selectInstances(eq("test-service"), eq("DEFAULT"), eq(true)))
				.thenReturn(instances);

		IClientConfig clientConfig = mock(IClientConfig.class);
		when(clientConfig.getClientName()).thenReturn("test-service");
		NacosServerList serverList = new NacosServerList(nacosDiscoveryProperties);
		serverList.initWithNiwsConfig(clientConfig);
		List<NacosServer> servers = serverList.getInitialListOfServers();
		assertThat(servers).hasSize(1);

		servers = serverList.getUpdatedListOfServers();
		assertThat(servers).hasSize(1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetServersWithInstanceStatus() throws Exception {
		ArrayList<Instance> instances = new ArrayList<>();

		HashMap<String, String> map1 = new HashMap<>();
		map1.put("instanceNum", "1");
		HashMap<String, String> map2 = new HashMap<>();
		map2.put("instanceNum", "2");
		instances.add(NacosMockTest.serviceInstance("test-service", false, map1));
		instances.add(NacosMockTest.serviceInstance("test-service", true, map2));

		NacosDiscoveryProperties nacosDiscoveryProperties = mock(
				NacosDiscoveryProperties.class);

		NamingService namingService = mock(NamingService.class);

		when(nacosDiscoveryProperties.namingServiceInstance()).thenReturn(namingService);
		when(nacosDiscoveryProperties.getGroup()).thenReturn("DEFAULT");
		when(namingService.selectInstances(eq("test-service"), eq("DEFAULT"), eq(true)))
				.thenReturn(instances.stream().filter(Instance::isHealthy)
						.collect(Collectors.toList()));

		IClientConfig clientConfig = mock(IClientConfig.class);
		when(clientConfig.getClientName()).thenReturn("test-service");
		NacosServerList serverList = new NacosServerList(nacosDiscoveryProperties);
		serverList.initWithNiwsConfig(clientConfig);
		List<NacosServer> servers = serverList.getInitialListOfServers();
		assertThat(servers).hasSize(1);

		NacosServer nacosServer = servers.get(0);

		assertThat(nacosServer.getMetaInfo().getInstanceId())
				.isEqualTo(instances.get(1).getInstanceId());
		assertThat(nacosServer.getMetadata()).isEqualTo(map2);
		assertThat(nacosServer.getInstance().isHealthy()).isEqualTo(true);
		assertThat(nacosServer.getInstance().getServiceName()).isEqualTo("test-service");
		assertThat(nacosServer.getInstance().getMetadata().get("instanceNum"))
				.isEqualTo("2");

	}

	@Test
	public void testUpdateServers() throws Exception {
		ArrayList<Instance> instances = new ArrayList<>();

		HashMap<String, String> map = new HashMap<>();
		map.put("instanceNum", "1");
		instances.add(NacosMockTest.serviceInstance("test-service", true, map));

		NacosDiscoveryProperties nacosDiscoveryProperties = mock(
				NacosDiscoveryProperties.class);

		NamingService namingService = mock(NamingService.class);

		when(nacosDiscoveryProperties.namingServiceInstance()).thenReturn(namingService);
		when(nacosDiscoveryProperties.getGroup()).thenReturn("DEFAULT");
		when(namingService.selectInstances(eq("test-service"), eq("DEFAULT"), eq(true)))
				.thenReturn(instances.stream().filter(Instance::isHealthy)
						.collect(Collectors.toList()));

		IClientConfig clientConfig = mock(IClientConfig.class);
		when(clientConfig.getClientName()).thenReturn("test-service");
		NacosServerList serverList = new NacosServerList(nacosDiscoveryProperties);
		serverList.initWithNiwsConfig(clientConfig);

		List<NacosServer> servers = serverList.getUpdatedListOfServers();
		assertThat(servers).hasSize(1);

		assertThat(servers.get(0).getInstance().isHealthy()).isEqualTo(true);
		assertThat(servers.get(0).getInstance().getMetadata().get("instanceNum"))
				.isEqualTo("1");
	}

}
