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

import java.util.Objects;
import java.util.Properties;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
public class NacosNamingManager {

	private static final Logger log = LoggerFactory.getLogger(NacosNamingManager.class);

	private static NamingService namingService = null;

	private static NamingMaintainService namingMaintainService = null;

	private NacosDiscoveryProperties discoveryProperties;

	public NacosNamingManager(NacosDiscoveryProperties discoveryProperties) {
		this.discoveryProperties = discoveryProperties;
	}

	public NamingService getNamingService() {
		if (Objects.isNull(namingService)) {
			Properties nacosProperties = discoveryProperties.getNacosProperties();
			try {
				namingService = NacosFactory.createNamingService(nacosProperties);
			}
			catch (Exception e) {
				log.error("create naming service error! properties: {}", nacosProperties,
						e);
			}
		}
		return namingService;
	}

	public NamingMaintainService getNamingMaintainService() {
		if (Objects.isNull(namingMaintainService)) {
			Properties nacosProperties = discoveryProperties.getNacosProperties();
			try {
				namingMaintainService = NamingMaintainFactory
						.createMaintainService(nacosProperties);
			}
			catch (Exception e) {
				log.error("create naming service error! properties: {}", nacosProperties,
						e);
			}
		}
		return namingMaintainService;
	}

}
