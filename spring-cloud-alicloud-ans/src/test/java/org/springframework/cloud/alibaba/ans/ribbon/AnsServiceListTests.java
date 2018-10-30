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

package org.springframework.cloud.alibaba.ans.ribbon;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.cloud.alicloud.ans.ribbon.AnsServer;
import org.springframework.cloud.alicloud.ans.ribbon.AnsServerList;

import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;
import com.netflix.loadbalancer.Server;

/**
 * @author xiaolongzuo
 */
public class AnsServiceListTests {

	static final String IP_ADDR = "10.0.0.2";

	static final int PORT = 8080;

	@Test
	public void testAnsServer() {
		AnsServerList serverList = getAnsServerList();
		List<AnsServer> servers = serverList.getInitialListOfServers();
		assertNotNull("servers was null", servers);
		assertEquals("servers was not size 1", 1, servers.size());
		Server des = assertAnsServer(servers);
		assertEquals("hostPort was wrong", IP_ADDR + ":" + PORT, des.getHostPort());
	}

	protected Server assertAnsServer(List<AnsServer> servers) {
		Server actualServer = servers.get(0);
		assertTrue("server was not a DomainExtractingServer",
				actualServer instanceof AnsServer);
		AnsServer des = AnsServer.class.cast(actualServer);
		assertNotNull("host is null", des.getHealthService());
		assertEquals("unit was wrong", "DEFAULT", des.getHealthService().getUnit());
		return des;
	}

	protected AnsServerList getAnsServerList() {
		Host host = mock(Host.class);
		given(host.getIp()).willReturn(IP_ADDR);
		given(host.getDoubleWeight()).willReturn(1.0);
		given(host.getPort()).willReturn(PORT);
		given(host.getWeight()).willReturn(1);
		given(host.getUnit()).willReturn("DEFAULT");

		AnsServer server = new AnsServer(host, "testDom");
		@SuppressWarnings("unchecked")
		AnsServerList originalServerList = mock(AnsServerList.class);
		given(originalServerList.getInitialListOfServers())
				.willReturn(Arrays.asList(server));
		return originalServerList;
	}

}
