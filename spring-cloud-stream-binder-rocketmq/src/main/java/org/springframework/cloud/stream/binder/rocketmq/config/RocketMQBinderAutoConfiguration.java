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

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQMessageChannelBinder;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.cloud.stream.binder.rocketmq.metrics.RocketMQBinderMetrics;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQExtendedBindingProperties;
import org.springframework.cloud.stream.binder.rocketmq.provisioning.RocketMQTopicProvisioner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration
@Import({ RocketMQAutoConfiguration.class,
		RocketMQBinderHealthIndicatorAutoConfiguration.class })
@EnableConfigurationProperties({ RocketMQBinderConfigurationProperties.class,
		RocketMQExtendedBindingProperties.class })
public class RocketMQBinderAutoConfiguration {

	private final RocketMQExtendedBindingProperties extendedBindingProperties;

	private final RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties;

	@Autowired(required = false)
	private RocketMQProperties rocketMQProperties = new RocketMQProperties();

	@Autowired
	public RocketMQBinderAutoConfiguration(
			RocketMQExtendedBindingProperties extendedBindingProperties,
			RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties) {
		this.extendedBindingProperties = extendedBindingProperties;
		this.rocketBinderConfigurationProperties = rocketBinderConfigurationProperties;
	}

	@Bean
	public RocketMQTopicProvisioner provisioningProvider() {
		return new RocketMQTopicProvisioner();
	}

	@Bean
	public RocketMQMessageChannelBinder rocketMessageChannelBinder(
			RocketMQTopicProvisioner provisioningProvider,
			InstrumentationManager instrumentationManager) {
		RocketMQMessageChannelBinder binder = new RocketMQMessageChannelBinder(
				provisioningProvider, extendedBindingProperties,
				rocketBinderConfigurationProperties, rocketMQProperties,
				instrumentationManager);
		binder.setExtendedBindingProperties(extendedBindingProperties);
		return binder;
	}

	@Bean
	public InstrumentationManager instrumentationManager() {
		return new InstrumentationManager();
	}

	@Configuration
	@ConditionalOnClass(MeterRegistry.class)
	@ConditionalOnBean(MeterRegistry.class)
	protected class RocketMQBinderMetricsConfiguration {

		@Bean
		@ConditionalOnMissingBean(RocketMQBinderMetrics.class)
		public MeterBinder rocketMqBinderMetrics(
				RocketMQMessageChannelBinder rocketMQMessageChannelBinder,
				RocketMQBinderConfigurationProperties rocketMQBinderConfigurationProperties,
				MeterRegistry meterRegistry) {
			return new RocketMQBinderMetrics(rocketMQMessageChannelBinder,
					rocketMQBinderConfigurationProperties, meterRegistry);
		}

	}

}
