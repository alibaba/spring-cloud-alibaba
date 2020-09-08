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

package com.alibaba.cloud.sidecar.nacos;

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.sidecar.SidecarDiscoveryClient;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author www.itmuch.com
 */
public class SidecarNacosDiscoveryClient implements SidecarDiscoveryClient {

	private static final Logger log = LoggerFactory
			.getLogger(SidecarNacosDiscoveryClient.class);

	private NacosServiceManager nacosServiceManager;

	private final SidecarNacosDiscoveryProperties sidecarNacosDiscoveryProperties;

	public SidecarNacosDiscoveryClient(NacosServiceManager nacosServiceManager,
			SidecarNacosDiscoveryProperties sidecarNacosDiscoveryProperties) {
		this.nacosServiceManager = nacosServiceManager;
		this.sidecarNacosDiscoveryProperties = sidecarNacosDiscoveryProperties;
	}

	@Override
	public void registerInstance(String applicationName, String ip, Integer port) {
		try {
			this.namingService().registerInstance(applicationName,
					sidecarNacosDiscoveryProperties.getGroup(), ip, port);
		}
		catch (NacosException e) {
			log.warn("nacos exception happens", e);
		}
	}

	@Override
	public void deregisterInstance(String applicationName, String ip, Integer port) {
		try {
			this.namingService().deregisterInstance(applicationName,
					sidecarNacosDiscoveryProperties.getGroup(), ip, port);
		}
		catch (NacosException e) {
			log.warn("nacos exception happens", e);
		}
	}

	private NamingService namingService() {
		return nacosServiceManager
				.getNamingService(sidecarNacosDiscoveryProperties.getNacosProperties());
	}

}
