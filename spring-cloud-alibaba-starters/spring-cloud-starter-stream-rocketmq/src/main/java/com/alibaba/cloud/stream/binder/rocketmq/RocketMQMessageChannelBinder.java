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

package com.alibaba.cloud.stream.binder.rocketmq;

import com.alibaba.cloud.stream.binder.rocketmq.custom.RocketMQBeanContainerCache;
import com.alibaba.cloud.stream.binder.rocketmq.extend.ErrorAcknowledgeHandler;
import com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter;
import com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.pull.DefaultErrorAcknowledgeHandler;
import com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.pull.RocketMQMessageSource;
import com.alibaba.cloud.stream.binder.rocketmq.integration.outbound.RocketMQProducerMessageHandler;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQExtendedBindingProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.provisioning.RocketMQTopicProvisioner;
import com.alibaba.cloud.stream.binder.rocketmq.utils.RocketMQUtils;
import org.apache.rocketmq.common.protocol.NamespaceUtil;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.binding.MessageConverterConfigurer;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.StaticMessageHeaderAccessor;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.support.DefaultErrorMessageStrategy;
import org.springframework.integration.support.ErrorMessageStrategy;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.util.StringUtils;

/**
 * A {@link org.springframework.cloud.stream.binder.Binder} that uses RocketMQ as the
 * underlying middleware.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageChannelBinder extends
		AbstractMessageChannelBinder<ExtendedConsumerProperties<RocketMQConsumerProperties>, ExtendedProducerProperties<RocketMQProducerProperties>, RocketMQTopicProvisioner>
		implements
		ExtendedPropertiesBinder<MessageChannel, RocketMQConsumerProperties, RocketMQProducerProperties> {

	private final RocketMQExtendedBindingProperties extendedBindingProperties;

	private final RocketMQBinderConfigurationProperties binderConfigurationProperties;

	public RocketMQMessageChannelBinder(
			RocketMQBinderConfigurationProperties binderConfigurationProperties,
			RocketMQExtendedBindingProperties extendedBindingProperties,
			RocketMQTopicProvisioner provisioningProvider) {
		super(new String[0], provisioningProvider);
		this.extendedBindingProperties = extendedBindingProperties;
		this.binderConfigurationProperties = binderConfigurationProperties;
	}

	@Override
	protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
			ExtendedProducerProperties<RocketMQProducerProperties> extendedProducerProperties,
			MessageChannel channel, MessageChannel errorChannel) throws Exception {
		if (!extendedProducerProperties.getExtension().getEnabled()) {
			throw new RuntimeException("Binding for channel " + destination.getName()
					+ " has been disabled, message can't be delivered");
		}
		RocketMQProducerProperties mqProducerProperties = RocketMQUtils
				.mergeRocketMQProperties(binderConfigurationProperties,
						extendedProducerProperties.getExtension());
		RocketMQProducerMessageHandler messageHandler = new RocketMQProducerMessageHandler(
				destination, extendedProducerProperties, mqProducerProperties);
		messageHandler.setApplicationContext(this.getApplicationContext());
		if (errorChannel != null) {
			messageHandler.setSendFailureChannel(errorChannel);
		}
		MessageConverterConfigurer.PartitioningInterceptor partitioningInterceptor = ((AbstractMessageChannel) channel)
				.getInterceptors().stream()
				.filter(channelInterceptor -> channelInterceptor instanceof MessageConverterConfigurer.PartitioningInterceptor)
				.map(channelInterceptor -> ((MessageConverterConfigurer.PartitioningInterceptor) channelInterceptor))
				.findFirst().orElse(null);
		messageHandler.setPartitioningInterceptor(partitioningInterceptor);
		messageHandler.setBeanFactory(this.getApplicationContext().getBeanFactory());
		messageHandler.setErrorMessageStrategy(this.getErrorMessageStrategy());
		return messageHandler;
	}

	@Override
	protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
			ExtendedProducerProperties<RocketMQProducerProperties> producerProperties,
			MessageChannel errorChannel) throws Exception {
		throw new UnsupportedOperationException(
				"The abstract binder should not call this method");
	}

	@Override
	protected MessageProducer createConsumerEndpoint(ConsumerDestination destination,
			String group,
			ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties)
			throws Exception {
		boolean anonymous = !StringUtils.hasLength(group);
		/***
		 * When using DLQ, at least the group property must be provided for proper naming
		 * of the DLQ destination According to
		 * https://docs.spring.io/spring-cloud-stream/docs/3.2.1/reference/html/spring-cloud-stream.html#spring-cloud-stream-reference
		 */
		if (anonymous && NamespaceUtil.isDLQTopic(destination.getName())) {
			throw new RuntimeException(
					"group must be configured for DLQ" + destination.getName());
		}
		group = anonymous ? RocketMQUtils.anonymousGroup(destination.getName()) : group;

		RocketMQUtils.mergeRocketMQProperties(binderConfigurationProperties,
				extendedConsumerProperties.getExtension());
		extendedConsumerProperties.getExtension().setGroup(group);

		RocketMQInboundChannelAdapter inboundChannelAdapter = new RocketMQInboundChannelAdapter(
				destination.getName(), extendedConsumerProperties);
		ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination,
				group, extendedConsumerProperties);
		if (extendedConsumerProperties.getMaxAttempts() > 1) {
			inboundChannelAdapter
					.setRetryTemplate(buildRetryTemplate(extendedConsumerProperties));
			inboundChannelAdapter.setRecoveryCallback(errorInfrastructure.getRecoverer());
		}
		else {
			inboundChannelAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
		}
		return inboundChannelAdapter;
	}

	@Override
	protected PolledConsumerResources createPolledConsumerResources(String name,
			String group, ConsumerDestination destination,
			ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties) {
		RocketMQUtils.mergeRocketMQProperties(binderConfigurationProperties,
				extendedConsumerProperties.getExtension());
		extendedConsumerProperties.getExtension().setGroup(group);
		RocketMQMessageSource messageSource = new RocketMQMessageSource(name,
				extendedConsumerProperties);
		return new PolledConsumerResources(messageSource, registerErrorInfrastructure(
				destination, group, extendedConsumerProperties, true));
	}

	@Override
	protected MessageHandler getPolledConsumerErrorMessageHandler(
			ConsumerDestination destination, String group,
			ExtendedConsumerProperties<RocketMQConsumerProperties> properties) {
		return message -> {
			if (message.getPayload() instanceof MessagingException) {
				AcknowledgmentCallback ack = StaticMessageHeaderAccessor
						.getAcknowledgmentCallback(
								((MessagingException) message.getPayload())
										.getFailedMessage());
				if (ack != null) {
					ErrorAcknowledgeHandler handler = RocketMQBeanContainerCache.getBean(
							properties.getExtension().getPull().getErrAcknowledge(),
							ErrorAcknowledgeHandler.class,
							new DefaultErrorAcknowledgeHandler());
					ack.acknowledge(
							handler.handler(((MessagingException) message.getPayload())
									.getFailedMessage()));
				}
			}
		};
	}

	/**
	 * Binders can return an {@link ErrorMessageStrategy} for building error messages;
	 * binder implementations typically might add extra headers to the error message.
	 * @return the implementation - may be null.
	 */
	@Override
	protected ErrorMessageStrategy getErrorMessageStrategy() {
		// It can be extended to custom if necessary.
		return new DefaultErrorMessageStrategy();
	}

	@Override
	public RocketMQConsumerProperties getExtendedConsumerProperties(String channelName) {
		return this.extendedBindingProperties.getExtendedConsumerProperties(channelName);
	}

	@Override
	public RocketMQProducerProperties getExtendedProducerProperties(String channelName) {
		return this.extendedBindingProperties.getExtendedProducerProperties(channelName);
	}

	@Override
	public String getDefaultsPrefix() {
		return this.extendedBindingProperties.getDefaultsPrefix();
	}

	@Override
	public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
		return this.extendedBindingProperties.getExtendedPropertiesEntryClass();
	}

}
