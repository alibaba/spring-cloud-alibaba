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

package org.springframework.cloud.alibaba.nacos.ribbon;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.loadbalancer.Server;

import java.util.Map;

/**
 * @author xiaojing
 */
public class NacosServer extends Server {

	private final MetaInfo metaInfo;
	private final Instance instance;
	private final Map<String, String> metadata;

	public NacosServer(final Instance instance) {
		super(instance.getIp(), instance.getPort());
		this.instance = instance;
		this.metaInfo = new MetaInfo() {
			@Override
			public String getAppName() {
				return instance.getServiceName();
			}

			@Override
			public String getServerGroup() {
				return null;
			}

			@Override
			public String getServiceIdForDiscovery() {
				return null;
			}

			@Override
			public String getInstanceId() {
				return instance.getInstanceId();
			}
		};
		this.metadata = instance.getMetadata();
	}

	@Override
	public MetaInfo getMetaInfo() {
		return metaInfo;
	}

	public Instance getInstance() {
		return instance;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}
}
