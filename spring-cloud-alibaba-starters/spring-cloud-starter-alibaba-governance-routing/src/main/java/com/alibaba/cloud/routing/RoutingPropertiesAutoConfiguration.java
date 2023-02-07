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

package com.alibaba.cloud.routing;

import com.alibaba.cloud.routing.publish.TargetServiceChangedPublisher;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author HH
 * @since 2.2.10-RC1
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ RoutingProperties.class })
public class RoutingPropertiesAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public RoutingProperties routingProperties() {
		return new RoutingProperties();
	}

	@Bean
	public TargetServiceChangedPublisher targetServiceChangedPublisher() {
		return new TargetServiceChangedPublisher();
	}

}
