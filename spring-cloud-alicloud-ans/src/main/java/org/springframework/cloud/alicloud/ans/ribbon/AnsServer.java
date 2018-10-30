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

import java.util.Collections;
import java.util.Map;

import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;
import com.netflix.loadbalancer.Server;

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
		this.metadata = Collections.emptyMap();
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
				return null;
			}

			@Override
			public String getInstanceId() {
				return null;
			}
		};
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

}
