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

package org.springframework.cloud.stream.binder.rocketmq;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.LocalTransactionExecuter;
import org.apache.rocketmq.client.producer.TransactionCheckListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.binder.rocketmq.consuming.ConsumersManager;
import org.springframework.cloud.stream.binder.rocketmq.integration.RocketMQInboundChannelAdapter;
import org.springframework.cloud.stream.binder.rocketmq.integration.RocketMQMessageHandler;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQExtendedBindingProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import org.springframework.cloud.stream.binder.rocketmq.provisioning.RocketMQTopicProvisioner;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.ClassUtils;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageChannelBinder extends
		AbstractMessageChannelBinder<ExtendedConsumerProperties<RocketMQConsumerProperties>, ExtendedProducerProperties<RocketMQProducerProperties>, RocketMQTopicProvisioner>
		implements
		ExtendedPropertiesBinder<MessageChannel, RocketMQConsumerProperties, RocketMQProducerProperties> {

	private static final Logger logger = LoggerFactory
			.getLogger(RocketMQMessageChannelBinder.class);

	private final RocketMQExtendedBindingProperties extendedBindingProperties;
	private final RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties;
	private final InstrumentationManager instrumentationManager;
	private final ConsumersManager consumersManager;

	public RocketMQMessageChannelBinder(ConsumersManager consumersManager,
			RocketMQExtendedBindingProperties extendedBindingProperties,
			RocketMQTopicProvisioner provisioningProvider,
			RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties,
			InstrumentationManager instrumentationManager) {
		super(null, provisioningProvider);
		this.consumersManager = consumersManager;
		this.extendedBindingProperties = extendedBindingProperties;
		this.rocketBinderConfigurationProperties = rocketBinderConfigurationProperties;
		this.instrumentationManager = instrumentationManager;
	}

	@Override
	protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
			ExtendedProducerProperties<RocketMQProducerProperties> producerProperties,
			MessageChannel errorChannel) throws Exception {
		if (producerProperties.getExtension().getEnabled()) {
			RocketMQMessageHandler messageHandler = new RocketMQMessageHandler(
					destination.getName(), producerProperties,
					rocketBinderConfigurationProperties, instrumentationManager);
			if (producerProperties.getExtension().getTransactional()) {
				// transaction message check LocalTransactionExecuter
				messageHandler.setLocalTransactionExecuter(
						getClassConfiguration(destination.getName(),
								producerProperties.getExtension().getExecuter(),
								LocalTransactionExecuter.class));
				// transaction message check TransactionCheckListener
				messageHandler.setTransactionCheckListener(
						getClassConfiguration(destination.getName(),
								producerProperties.getExtension()
										.getTransactionCheckListener(),
								TransactionCheckListener.class));
			}

			return messageHandler;
		}
		else {
			throw new RuntimeException("Binding for channel " + destination.getName()
					+ " has been disabled, message can't be delivered");
		}
	}

	@Override
	protected MessageProducer createConsumerEndpoint(ConsumerDestination destination,
			String group,
			ExtendedConsumerProperties<RocketMQConsumerProperties> consumerProperties)
			throws Exception {
		if (group == null || "".equals(group)) {
			throw new RuntimeException(
					"'group must be configured for channel + " + destination.getName());
		}

		RocketMQInboundChannelAdapter rocketInboundChannelAdapter = new RocketMQInboundChannelAdapter(
				consumersManager, consumerProperties, destination.getName(), group,
				instrumentationManager);

		ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination,
				group, consumerProperties);
		if (consumerProperties.getMaxAttempts() > 1) {
			rocketInboundChannelAdapter
					.setRetryTemplate(buildRetryTemplate(consumerProperties));
			rocketInboundChannelAdapter
					.setRecoveryCallback(errorInfrastructure.getRecoverer());
		}
		else {
			rocketInboundChannelAdapter
					.setErrorChannel(errorInfrastructure.getErrorChannel());
		}

		return rocketInboundChannelAdapter;
	}

	@Override
	public RocketMQConsumerProperties getExtendedConsumerProperties(String channelName) {
		return extendedBindingProperties.getExtendedConsumerProperties(channelName);
	}

	@Override
	public RocketMQProducerProperties getExtendedProducerProperties(String channelName) {
		return extendedBindingProperties.getExtendedProducerProperties(channelName);
	}

	private <T> T getClassConfiguration(String destName, String className,
			Class<T> interfaceClass) {
		if (StringUtils.isEmpty(className)) {
			throw new RuntimeException("Binding for channel " + destName
					+ " using transactional message, should set "
					+ interfaceClass.getSimpleName() + " configuration"
					+ interfaceClass.getSimpleName() + " should be set, like "
					+ "'spring.cloud.stream.rocketmq.bindings.output.producer.xxx=TheFullClassNameOfYour"
					+ interfaceClass.getSimpleName() + "'");
		}
		else if (StringUtils.isNotEmpty(className)) {
			Class fieldClass;
			// check class exists
			try {
				fieldClass = ClassUtils.forName(className,
						RocketMQMessageChannelBinder.class.getClassLoader());
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException("Binding for channel " + destName
						+ " using transactional message, but " + className
						+ " class is not found");
			}
			// check interface incompatible
			if (!interfaceClass.isAssignableFrom(fieldClass)) {
				throw new RuntimeException("Binding for channel " + destName
						+ " using transactional message, but " + className
						+ " is incompatible with " + interfaceClass.getSimpleName()
						+ " interface");
			}
			try {
				return (T) fieldClass.newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException("Binding for channel " + destName
						+ " using transactional message, but " + className
						+ " instance error", e);
			}
		}
		return null;
	}

}
