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

package com.alibaba.cloud.stream.binder.rocketmq;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

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
import com.alibaba.cloud.stream.binder.rocketmq.support.MessageCollector;
import com.alibaba.cloud.stream.binder.rocketmq.utils.RocketMQUtils;
import org.apache.rocketmq.common.protocol.NamespaceUtil;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.binding.MessageConverterConfigurer;
import org.springframework.cloud.stream.converter.CompositeMessageConverterFactory;
import org.springframework.cloud.stream.converter.MessageConverterUtils;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.StaticMessageHeaderAccessor;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.support.DefaultErrorMessageStrategy;
import org.springframework.integration.support.ErrorMessageStrategy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
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
		 * 	When using DLQ, at least the group property must be provided for proper naming of the DLQ destination
		 *  According to https://docs.spring.io/spring-cloud-stream/docs/3.2.1/reference/html/spring-cloud-stream.html#spring-cloud-stream-reference
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

	private static final class InboundMessageConvertingInterceptor implements ChannelInterceptor {
		private final DefaultContentTypeResolver contentTypeResolver;
		private final CompositeMessageConverterFactory converterFactory;

		private InboundMessageConvertingInterceptor() {
			this.contentTypeResolver = new DefaultContentTypeResolver();
			this.converterFactory = new CompositeMessageConverterFactory();
		}

		private static boolean equalTypeAndSubType(MimeType m1, MimeType m2) {
			return m1 != null && m2 != null && m1.getType().equalsIgnoreCase(m2.getType()) && m1.getSubtype().equalsIgnoreCase(m2.getSubtype());
		}

		public Message<?> preSend(Message<?> message, MessageChannel channel) {
			Class<?> targetClass = null;
			MessageConverter converter = null;
			MimeType contentType = MimeType.valueOf(this.contentTypeResolver.resolve(message.getHeaders()).toString());
			if (contentType != null && (equalTypeAndSubType(MessageConverterUtils.X_JAVA_SERIALIZED_OBJECT, contentType) || equalTypeAndSubType(MessageConverterUtils.X_JAVA_OBJECT, contentType))) {
				message = MessageBuilder.fromMessage(message).setHeader("contentType", contentType).build();
				converter = equalTypeAndSubType(MessageConverterUtils.X_JAVA_SERIALIZED_OBJECT, contentType) ? this.converterFactory.getMessageConverterForType(contentType) : this.converterFactory.getMessageConverterForAllRegistered();
				String targetClassName = contentType.getParameter("type");
				if (StringUtils.hasText(targetClassName)) {
					try {
						targetClass = Class.forName(targetClassName, false, Thread.currentThread().getContextClassLoader());
					} catch (Exception var8) {
						throw new IllegalStateException("Failed to determine class name for contentType: " + message.getHeaders(), var8);
					}
				}
			}

			Object payload;
			if (converter != null) {
				Assert.isTrue(!equalTypeAndSubType(MessageConverterUtils.X_JAVA_OBJECT, contentType) || targetClass != null, "Cannot deserialize into message since 'contentType` is not encoded with the actual target type.Consider 'application/x-java-object; type=foo.bar.MyClass'");
				payload = converter.fromMessage(message, targetClass);
			} else {
				MimeType deserializeContentType = this.contentTypeResolver.resolve(message.getHeaders());
				if (deserializeContentType == null) {
					deserializeContentType = contentType;
				}

				payload = deserializeContentType == null ? message.getPayload() : this.deserializePayload(message.getPayload(), deserializeContentType);
			}

			message = MessageBuilder.withPayload(payload).copyHeaders(message.getHeaders()).setHeader("contentType", contentType).build();
			return message;
		}

		private Object deserializePayload(Object payload, MimeType contentType) {
			if (payload instanceof byte[] && ("text".equalsIgnoreCase(contentType.getType()) || equalTypeAndSubType(MimeTypeUtils.APPLICATION_JSON, contentType))) {
				payload = new String((byte[])((byte[])payload), StandardCharsets.UTF_8);
			}

			return payload;
		}
	}


	private static class MessageCollectorImpl implements MessageCollector {
		private final Map<MessageChannel, BlockingQueue<Message<?>>> results;

		private MessageCollectorImpl() {
			this.results = new HashMap();
		}

		private BlockingQueue<Message<?>> register(MessageChannel channel, boolean useNativeEncoding) {
			if (!useNativeEncoding) {
				((AbstractMessageChannel)channel).addInterceptor(new InboundMessageConvertingInterceptor());
			}

			LinkedBlockingDeque<Message<?>> result = new LinkedBlockingDeque();
			Assert.isTrue(!this.results.containsKey(channel), "Channel [" + channel + "] was already bound");
			this.results.put(channel, result);
			return result;
		}

		private void unregister(MessageChannel channel) {
			Assert.notNull(this.results.remove(channel), "Trying to unregister a mapping for an unknown channel [" + channel + "]");
		}

		public BlockingQueue<Message<?>> forChannel(MessageChannel channel) {
			BlockingQueue<Message<?>> queue = (BlockingQueue)this.results.get(channel);
			Assert.notNull(queue, "Channel [" + channel + "] was not bound by " + RocketMQMessageChannelBinder.class);
			return queue;
		}
	}
}
