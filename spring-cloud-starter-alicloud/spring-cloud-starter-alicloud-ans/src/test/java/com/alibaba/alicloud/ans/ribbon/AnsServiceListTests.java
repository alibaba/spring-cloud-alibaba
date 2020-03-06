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

package com.alibaba.alicloud.ans.ribbon;

import java.util.Arrays;
import java.util.List;

import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;
import com.netflix.loadbalancer.Server;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
		assertThat(servers).isNotNull();
		assertThat(servers.size()).isEqualTo(1);
		Server des = assertAnsServer(servers);
		assertThat(des.getHostPort()).isEqualTo(IP_ADDR + ":" + PORT);
	}

	protected Server assertAnsServer(List<AnsServer> servers) {
		Server actualServer = servers.get(0);
		assertThat(actualServer instanceof AnsServer).isEqualTo(Boolean.TRUE);
		AnsServer des = AnsServer.class.cast(actualServer);
		assertThat(des.getHealthService()).isNotNull();
		assertThat(des.getHealthService().getUnit()).isEqualTo("DEFAULT");
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
