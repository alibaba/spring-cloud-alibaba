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

package com.alibaba.cloud.sentinel.endpoint;

import com.alibaba.cloud.sentinel.SentinelProperties;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author hengyunabc
 */
@ConditionalOnClass(Endpoint.class)
@EnableConfigurationProperties({ SentinelProperties.class })
public class SentinelEndpointAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnEnabledEndpoint
	public SentinelEndpoint sentinelEndPoint(SentinelProperties sentinelProperties) {
		return new SentinelEndpoint(sentinelProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnEnabledHealthIndicator("sentinel")
	public SentinelHealthIndicator sentinelHealthIndicator(
			DefaultListableBeanFactory beanFactory,
			SentinelProperties sentinelProperties) {
		return new SentinelHealthIndicator(beanFactory, sentinelProperties);
	}

}
