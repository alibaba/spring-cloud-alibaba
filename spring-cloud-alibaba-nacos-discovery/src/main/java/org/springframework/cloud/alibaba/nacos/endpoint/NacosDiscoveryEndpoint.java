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

package org.springframework.cloud.alibaba.nacos.endpoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;

/**
 * Endpoint for nacos discovery, get nacos properties and subscribed services
 * @author xiaojing
 */
@Endpoint(id = "nacos-discovery")
public class NacosDiscoveryEndpoint {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NacosDiscoveryEndpoint.class);

	@Autowired
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	/**
	 * @return nacos discovery endpoint
	 */
	@ReadOperation
	public Map<String, Object> nacosDiscovery() {
		Map<String, Object> result = new HashMap<>();
		result.put("NacosDiscoveryProperties", nacosDiscoveryProperties);

		NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
		List<ServiceInfo> subscribe = Collections.emptyList();

		try {
			subscribe = namingService.getSubscribeServices();
		}
		catch (Exception e) {
			LOGGER.error("get subscribe services from nacos fail,", e);
		}
		result.put("subscribe", subscribe);
		return result;
	}
}