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

import java.util.ArrayList;
import java.util.List;

import com.alibaba.ans.core.NamingService;
import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

/**
 * @author xiaolongzuo
 */
public class AnsServerList extends AbstractServerList<AnsServer> {

	private String dom;

	public AnsServerList(String dom) {
		this.dom = dom;
	}

	@Override
	public List<AnsServer> getInitialListOfServers() {
		try {
			List<Host> hosts = NamingService.getHosts(getDom());
			return hostsToServerList(hosts);
		}
		catch (Exception e) {
			throw new IllegalStateException("Can not get ans hosts, dom=" + getDom(), e);
		}
	}

	@Override
	public List<AnsServer> getUpdatedListOfServers() {
		return getInitialListOfServers();
	}

	private AnsServer hostToServer(Host host) {
		AnsServer server = new AnsServer(host, getDom());
		return server;
	}

	private List<AnsServer> hostsToServerList(List<Host> hosts) {
		List<AnsServer> result = new ArrayList<AnsServer>(hosts.size());
		for (Host host : hosts) {
			if (host.isValid()) {
				result.add(hostToServer(host));
			}
		}

		return result;
	}

	public String getDom() {
		return dom;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig iClientConfig) {
		this.dom = iClientConfig.getClientName();
	}
}
