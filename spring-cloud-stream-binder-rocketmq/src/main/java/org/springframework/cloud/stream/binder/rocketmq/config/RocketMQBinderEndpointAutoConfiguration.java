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

package org.springframework.cloud.stream.binder.rocketmq.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.stream.binder.rocketmq.actuator.RocketMQBinderEndpoint;
import org.springframework.cloud.stream.binder.rocketmq.actuator.RocketMQBinderHealthIndicator;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration
@AutoConfigureAfter(EndpointAutoConfiguration.class)
@ConditionalOnClass(Endpoint.class)
public class RocketMQBinderEndpointAutoConfiguration {

	@Bean
	public RocketMQBinderEndpoint rocketBinderEndpoint() {
		return new RocketMQBinderEndpoint();
	}

	@Bean
	public RocketMQBinderHealthIndicator rocketBinderHealthIndicator() {
		return new RocketMQBinderHealthIndicator();
	}

	@Bean
	@ConditionalOnClass(name = "com.codahale.metrics.Counter")
	public InstrumentationManager instrumentationManager() {
		return new InstrumentationManager();
	}

}
