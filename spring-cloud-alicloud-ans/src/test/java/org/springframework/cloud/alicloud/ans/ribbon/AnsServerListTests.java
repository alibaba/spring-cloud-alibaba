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

package org.springframework.cloud.alicloud.ans.ribbon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.alicloud.ans.test.AnsMockTest.hostInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.alibaba.ans.core.NamingService;
import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;

import com.netflix.client.config.IClientConfig;

/**
 * @author xiaojing
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ NamingService.class, AnsServer.class })
public class AnsServerListTests {

	@Test
	@SuppressWarnings("unchecked")
	public void testEmptyInstancesReturnsEmptyList() throws Exception {

		PowerMockito.mockStatic(NamingService.class);
		when(NamingService.getHosts(anyString())).thenReturn(Collections.EMPTY_LIST);

		IClientConfig clientConfig = mock(IClientConfig.class);
		when(clientConfig.getClientName()).thenReturn("test-service");
		AnsServerList serverList = new AnsServerList("test-service");
		serverList.initWithNiwsConfig(clientConfig);
		List<AnsServer> servers = serverList.getInitialListOfServers();
		assertThat(servers).isEmpty();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetServers() throws Exception {

		ArrayList<Host> hosts = new ArrayList<>();
		hosts.add(hostInstance("test-service", true, Collections.emptyMap()));

		PowerMockito.mockStatic(NamingService.class);
		when(NamingService.getHosts(anyString())).thenReturn(hosts);
		PowerMockito.stub(PowerMockito.method(AnsServer.class, "isAlive", long.class))
				.toReturn(true);

		IClientConfig clientConfig = mock(IClientConfig.class);
		when(clientConfig.getClientName()).thenReturn("test-service");
		AnsServerList serverList = new AnsServerList("test-service");
		serverList.initWithNiwsConfig(clientConfig);
		List<AnsServer> servers = serverList.getInitialListOfServers();
		assertThat(servers).hasSize(1);

		servers = serverList.getUpdatedListOfServers();
		assertThat(servers).hasSize(1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetServersWithInstanceStatus() throws Exception {
		ArrayList<Host> hosts = new ArrayList<>();

		HashMap<String, String> map1 = new HashMap<>();
		map1.put("instanceNum", "1");
		HashMap<String, String> map2 = new HashMap<>();
		map2.put("instanceNum", "2");
		hosts.add(hostInstance("test-service", false, map1));
		hosts.add(hostInstance("test-service", true, map2));

		PowerMockito.mockStatic(NamingService.class);
		when(NamingService.getHosts(eq("test-service"))).thenReturn(
				hosts.stream().filter(Host::isValid).collect(Collectors.toList()));

		PowerMockito.stub(PowerMockito.method(AnsServer.class, "isAlive", long.class))
				.toReturn(true);

		IClientConfig clientConfig = mock(IClientConfig.class);
		when(clientConfig.getClientName()).thenReturn("test-service");
		AnsServerList serverList = new AnsServerList("test-service");
		serverList.initWithNiwsConfig(clientConfig);
		List<AnsServer> servers = serverList.getInitialListOfServers();
		assertThat(servers).hasSize(1);

		AnsServer ansServer = servers.get(0);
		Host host = ansServer.getHealthService();

		assertThat(ansServer.getMetaInfo().getInstanceId()).isEqualTo(
				host.getIp() + ":" + host.getHostname() + ":" + host.getPort());
		assertThat(ansServer.getHealthService().isValid()).isEqualTo(true);
		assertThat(ansServer.getHealthService().getHostname()).isEqualTo("test-service");

	}

	@Test
	public void testUpdateServers() throws Exception {
		ArrayList<Host> hosts = new ArrayList<>();

		HashMap<String, String> map = new HashMap<>();
		map.put("instanceNum", "1");
		hosts.add(hostInstance("test-service", true, map));

		PowerMockito.mockStatic(NamingService.class);
		when(NamingService.getHosts(eq("test-service"))).thenReturn(
				hosts.stream().filter(Host::isValid).collect(Collectors.toList()));
		PowerMockito.stub(PowerMockito.method(AnsServer.class, "isAlive", long.class))
				.toReturn(true);

		IClientConfig clientConfig = mock(IClientConfig.class);
		when(clientConfig.getClientName()).thenReturn("test-service");
		AnsServerList serverList = new AnsServerList("test-service");
		serverList.initWithNiwsConfig(clientConfig);

		List<AnsServer> servers = serverList.getUpdatedListOfServers();
		assertThat(servers).hasSize(1);

		assertThat(servers.get(0).getHealthService().isValid()).isEqualTo(true);
	}
}