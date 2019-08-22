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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

import com.alibaba.alicloud.acm.AcmPropertySourceRepository;
import com.alibaba.alicloud.acm.refresh.AcmRefreshHistory;
import com.alibaba.alicloud.context.acm.AcmProperties;

/**
 * @author xiaojing
 */
@ConditionalOnWebApplication
@ConditionalOnClass(name = "org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration")
@ConditionalOnProperty(name = "spring.cloud.alicloud.acm.enabled", matchIfMissing = true)
public class AcmEndpointAutoConfiguration {

	@Autowired
	private AcmProperties acmProperties;

	@Autowired
	private AcmRefreshHistory acmRefreshHistory;

	@Autowired
	private AcmPropertySourceRepository acmPropertySourceRepository;

	@ConditionalOnMissingBean
	@ConditionalOnEnabledEndpoint
	@Bean
	public AcmEndpoint acmEndpoint() {
		return new AcmEndpoint(acmProperties, acmRefreshHistory,
				acmPropertySourceRepository);
	}

	@Bean
	@ConditionalOnMissingBean
	public AcmHealthIndicator acmHealthIndicator(AcmProperties acmProperties,
			AcmPropertySourceRepository acmPropertySourceRepository) {
		return new AcmHealthIndicator(acmProperties, acmPropertySourceRepository);
	}
}
