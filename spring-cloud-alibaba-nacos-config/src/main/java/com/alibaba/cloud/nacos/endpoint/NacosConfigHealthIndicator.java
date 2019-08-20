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

package com.alibaba.cloud.nacos.endpoint;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import com.alibaba.nacos.api.config.ConfigService;

/**
 * @author xiaojing
 */
public class NacosConfigHealthIndicator extends AbstractHealthIndicator {

	private final ConfigService configService;

	public NacosConfigHealthIndicator(ConfigService configService) {
		this.configService = configService;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		builder.up();

		String status = configService.getServerStatus();
		builder.status(status);
	}
}
