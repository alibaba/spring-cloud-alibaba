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

package com.alibaba.alicloud.acm.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.util.StringUtils;

import com.alibaba.alicloud.acm.AcmPropertySourceRepository;
import com.alibaba.alicloud.acm.bootstrap.AcmPropertySource;
import com.alibaba.alicloud.context.acm.AcmProperties;
import com.alibaba.edas.acm.ConfigService;

/**
 * @author leijuan
 * @author juven
 */
public class AcmHealthIndicator extends AbstractHealthIndicator {

	private final AcmProperties acmProperties;

	private final AcmPropertySourceRepository acmPropertySourceRepository;

	private final List<String> dataIds;

	public AcmHealthIndicator(AcmProperties acmProperties,
			AcmPropertySourceRepository acmPropertySourceRepository) {
		this.acmProperties = acmProperties;
		this.acmPropertySourceRepository = acmPropertySourceRepository;

		this.dataIds = new ArrayList<>();
		for (AcmPropertySource acmPropertySource : this.acmPropertySourceRepository
				.getAll()) {
			this.dataIds.add(acmPropertySource.getDataId());
		}
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		for (String dataId : dataIds) {
			try {
				String config = ConfigService.getConfig(dataId, acmProperties.getGroup(),
						acmProperties.getTimeout());
				if (StringUtils.isEmpty(config)) {
					builder.down().withDetail(String.format("dataId: '%s', group: '%s'",
							dataId, acmProperties.getGroup()), "config is empty");
				}
			}
			catch (Exception e) {
				builder.down().withDetail(String.format("dataId: '%s', group: '%s'",
						dataId, acmProperties.getGroup()), e.getMessage());
			}
		}
		builder.up().withDetail("dataIds", dataIds);
	}
}
