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

import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;
import com.netflix.loadbalancer.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaolongzuo
 */
public class AnsServer extends Server {

	private final MetaInfo metaInfo;
	private final Host host;
	private final Map<String, String> metadata;

	public AnsServer(final Host host, final String dom) {
		super(host.getIp(), host.getPort());
		this.host = host;
		this.metadata = new HashMap();
		this.metadata.put("source", "ANS");
		metaInfo = new MetaInfo() {
			@Override
			public String getAppName() {
				return dom;
			}

			@Override
			public String getServerGroup() {
				return getMetadata().get("group");
			}

			@Override
			public String getServiceIdForDiscovery() {
				return dom;
			}

			@Override
			public String getInstanceId() {
				return AnsServer.this.host.getIp() + ":" + dom + ":"
						+ AnsServer.this.host.getPort();
			}
		};
	}

	@Override
	public boolean isAlive() {

		return true;
	}

	/**
	 * 
	 * @param timeOut Unit: Seconds
	 * @return
	 */
	public boolean isAlive(long timeOut) {
		try {
			String hostName = this.host.getHostname();
			hostName = hostName != null && hostName.trim().length() > 0 ? hostName
					: this.host.getIp();
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(hostName, this.host.getPort()),
					(int) TimeUnit.SECONDS.toMillis(timeOut));
			socket.close();
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	@Override
	public MetaInfo getMetaInfo() {
		return metaInfo;
	}

	public Host getHealthService() {
		return this.host;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	@Override
	public String toString() {
		return "AnsServer{" + "metaInfo=" + metaInfo + ", host=" + host + ", metadata="
				+ metadata + '}';
	}
}
