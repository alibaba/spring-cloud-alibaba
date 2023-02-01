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

package com.alibaba.cloud.stream.binder.rocketmq.autoconfigurate;

import com.alibaba.cloud.stream.binder.rocketmq.RocketMQMessageChannelBinder;
import com.alibaba.cloud.stream.binder.rocketmq.actuator.RocketMQBinderHealthIndicator;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQExtendedBindingProperties;
import com.alibaba.cloud.stream.binder.rocketmq.provisioning.RocketMQTopicProvisioner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * issue:https://github.com/alibaba/spring-cloud-alibaba/issues/1681 .
 *
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @author freeman
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ RocketMQExtendedBindingProperties.class,
		RocketMQBinderConfigurationProperties.class })
public class RocketMQBinderAutoConfiguration {

	@Autowired
	private RocketMQExtendedBindingProperties extendedBindingProperties;

	@Autowired
	private RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties;

	@Bean
	public RocketMQTopicProvisioner rocketMQTopicProvisioner() {
		return new RocketMQTopicProvisioner();
	}

	@Bean
	public RocketMQMessageChannelBinder rocketMQMessageChannelBinder(
			RocketMQTopicProvisioner provisioningProvider) {
		return new RocketMQMessageChannelBinder(rocketBinderConfigurationProperties,
				extendedBindingProperties, provisioningProvider);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(HealthIndicator.class)
	@ConditionalOnEnabledHealthIndicator("rocketmq")
	static class RocketMQBinderHealthIndicatorConfiguration {

		@Bean
		public RocketMQBinderHealthIndicator rocketMQBinderHealthIndicator() {
			return new RocketMQBinderHealthIndicator();
		}

	}

}
