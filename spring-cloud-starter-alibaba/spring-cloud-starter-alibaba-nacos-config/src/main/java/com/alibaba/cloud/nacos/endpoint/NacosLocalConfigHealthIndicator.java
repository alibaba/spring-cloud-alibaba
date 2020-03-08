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

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link HealthIndicator} for Local Nacos Config.
 *
 * @author illlight
 */
public class NacosLocalConfigHealthIndicator extends AbstractHealthIndicator {

	private final NacosConfigProperties nacosConfigProperties;

	private final List<String> dataIds;

	private final ConfigService configService;

	public NacosLocalConfigHealthIndicator(NacosConfigProperties nacosConfigProperties, ConfigService configService) {
		this.nacosConfigProperties = nacosConfigProperties;
		this.configService = configService;

		this.dataIds = new ArrayList<>();
		for (NacosPropertySource nacosPropertySource : NacosPropertySourceRepository.getAll()) {
			this.dataIds.add(nacosPropertySource.getDataId());
		}
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		boolean find = false;
		for (String dataId : dataIds) {
			try {
				String config = configService.getConfig(dataId,
						nacosConfigProperties.getGroup(),
						nacosConfigProperties.getTimeout());
				if (StringUtils.isEmpty(config)) {
					builder.withDetail(String.format("dataId: '%s', group: '%s'",
							dataId, nacosConfigProperties.getGroup()), "config isEmpty");
				} else {
					find = true;
					builder.withDetail(String.format("dataId: '%s', group: '%s'",
							dataId, nacosConfigProperties.getGroup()), "config exist");
				}
			} catch (Exception e) {
				builder.withDetail(String.format("dataId: '%s', group: '%s'",
						dataId, nacosConfigProperties.getGroup()), "get config exception");
			}
		}
		if (find) {
			builder.up();
		} else {
			builder.down();
		}
	}

}
