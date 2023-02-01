/*
 * Copyright 2013-2023 the original author or authors.
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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.alibaba.nacos.api.NacosFactory.createMaintainService;
import static com.alibaba.nacos.api.NacosFactory.createNamingService;

/**
 * @author yuhuangbin
 */
public class NacosServiceManager {

	private static final Logger log = LoggerFactory.getLogger(NacosServiceManager.class);

	private NacosDiscoveryProperties nacosDiscoveryProperties;

	private volatile NamingService namingService;

	private volatile NamingMaintainService namingMaintainService;

	public NamingService getNamingService() {
		if (Objects.isNull(this.namingService)) {
			buildNamingService(nacosDiscoveryProperties.getNacosProperties());
		}
		return namingService;
	}

	@Deprecated
	public NamingService getNamingService(Properties properties) {
		if (Objects.isNull(this.namingService)) {
			buildNamingService(properties);
		}
		return namingService;
	}

	public NamingMaintainService getNamingMaintainService(Properties properties) {
		if (Objects.isNull(namingMaintainService)) {
			buildNamingMaintainService(properties);
		}
		return namingMaintainService;
	}

	public boolean isNacosDiscoveryInfoChanged(
			NacosDiscoveryProperties currentNacosDiscoveryPropertiesCache) {
		if (Objects.isNull(this.nacosDiscoveryProperties)
				|| this.nacosDiscoveryProperties.equals(currentNacosDiscoveryPropertiesCache)) {
			return false;
		}
		return true;
	}

	private NamingMaintainService buildNamingMaintainService(Properties properties) {
		if (Objects.isNull(namingMaintainService)) {
			synchronized (NacosServiceManager.class) {
				if (Objects.isNull(namingMaintainService)) {
					namingMaintainService = createNamingMaintainService(properties);
				}
			}
		}
		return namingMaintainService;
	}

	private NamingService buildNamingService(Properties properties) {
		if (Objects.isNull(namingService)) {
			synchronized (NacosServiceManager.class) {
				if (Objects.isNull(namingService)) {
					namingService = createNewNamingService(properties);
				}
			}
		}
		return namingService;
	}

	private NamingService createNewNamingService(Properties properties) {
		try {
			return createNamingService(properties);
		}
		catch (NacosException e) {
			throw new RuntimeException(e);
		}
	}

	private NamingMaintainService createNamingMaintainService(Properties properties) {
		try {
			return createMaintainService(properties);
		}
		catch (NacosException e) {
			throw new RuntimeException(e);
		}
	}

	public void nacosServiceShutDown() throws NacosException {
		if (Objects.nonNull(this.namingService)) {
			this.namingService.shutDown();
			this.namingService = null;
		}
		if (Objects.nonNull(this.namingMaintainService)) {
			this.namingMaintainService.shutDown();
			this.namingMaintainService = null;
		}
	}

	public void setNacosDiscoveryProperties(NacosDiscoveryProperties nacosDiscoveryProperties) {
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
	}
}
