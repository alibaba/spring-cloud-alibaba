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

package com.alibaba.cloud.nacos.endpoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import com.alibaba.boot.nacos.common.PropertiesUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.spring.factory.CacheableEventPublishingNacosServiceFactory;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.metadata.NacosServiceMetaData;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * @author xiaojing
 */
public class NacosConfigHealthIndicator extends AbstractHealthIndicator {

	private static final String UP_STATUS = "up";

	private final ConfigService configService;

	public NacosConfigHealthIndicator(ConfigService configService) {
		this.configService = configService;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		builder.up();
		String status = configService.getServerStatus();
		NacosServiceFactory nacosServiceFactory = CacheableEventPublishingNacosServiceFactory
				.getSingleton();
		Collection<ConfigService> configServices = new ArrayList<>(
				nacosServiceFactory.getConfigServices());
		configServices.add(configService);
		for (ConfigService configService : configServices) {
			if (configService instanceof NacosServiceMetaData) {
				NacosServiceMetaData nacosServiceMetaData = (NacosServiceMetaData) configService;
				Properties properties = nacosServiceMetaData.getProperties();
				builder.withDetail(
						JSON.toJSONString(
								PropertiesUtils.extractSafeProperties(properties)),
						configService.getServerStatus());
			}
			if (!configService.getServerStatus().toLowerCase().equals(UP_STATUS)) {
				builder.down();
			}
		}
		builder.status(status);
	}

}
