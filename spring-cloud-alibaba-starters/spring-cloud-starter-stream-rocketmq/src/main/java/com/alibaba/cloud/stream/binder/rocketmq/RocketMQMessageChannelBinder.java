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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.cloud.stream.binder.rocketmq.consuming.RocketMQListenerBindingContainer;
import com.alibaba.cloud.stream.binder.rocketmq.integration.RocketMQInboundChannelAdapter;
import com.alibaba.cloud.stream.binder.rocketmq.integration.RocketMQMessageHandler;
import com.alibaba.cloud.stream.binder.rocketmq.integration.RocketMQMessageSource;
import com.alibaba.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQExtendedBindingProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.provisioning.RocketMQTopicProvisioner;
import com.alibaba.cloud.stream.binder.rocketmq.provisioning.selector.PartitionMessageQueueSelector;
import com.alibaba.cloud.stream.binder.rocketmq.support.JacksonRocketMQHeaderMapper;
import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQHeaderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQUtil;

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
import org.springframework.integration.acks.AcknowledgmentCallback.Status;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageChannelBinder extends
		AbstractMessageChannelBinder<ExtendedConsumerProperties<RocketMQConsumerProperties>, ExtendedProducerProperties<RocketMQProducerProperties>, RocketMQTopicProvisioner>
		implements
		ExtendedPropertiesBinder<MessageChannel, RocketMQConsumerProperties, RocketMQProducerProperties> {

	private RocketMQExtendedBindingProperties extendedBindingProperties = new RocketMQExtendedBindingProperties();

	private final RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties;

	private final RocketMQProperties rocketMQProperties;

	private final InstrumentationManager instrumentationManager;

	private Map<String, String> topicInUse = new HashMap<>();

	public RocketMQMessageChannelBinder(RocketMQTopicProvisioner provisioningProvider,
			RocketMQExtendedBindingProperties extendedBindingProperties,
			RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties,
			RocketMQProperties rocketMQProperties,
			InstrumentationManager instrumentationManager) {
		super(null, provisioningProvider);
		this.extendedBindingProperties = extendedBindingProperties;
		this.rocketBinderConfigurationProperties = rocketBinderConfigurationProperties;
		this.rocketMQProperties = rocketMQProperties;
		this.instrumentationManager = instrumentationManager;
	}

	@Override
	protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
			ExtendedProducerProperties<RocketMQProducerProperties> producerProperties,
			MessageChannel channel, MessageChannel errorChannel) throws Exception {
		if (producerProperties.getExtension().getEnabled()) {

			// if producerGroup is empty, using destination
			String extendedProducerGroup = producerProperties.getExtension().getGroup();
			String producerGroup = StringUtils.isEmpty(extendedProducerGroup)
					? destination.getName() : extendedProducerGroup;

			RocketMQBinderConfigurationProperties mergedProperties = RocketMQBinderUtils
					.mergeProperties(rocketBinderConfigurationProperties,
							rocketMQProperties);

			RocketMQTemplate rocketMQTemplate;
			if (producerProperties.getExtension().getTransactional()) {
				Map<String, RocketMQTemplate> rocketMQTemplates = getBeanFactory()
						.getBeansOfType(RocketMQTemplate.class);
				if (rocketMQTemplates.size() == 0) {
					throw new IllegalStateException(
							"there is no RocketMQTemplate in Spring BeanFactory");
				}
				else if (rocketMQTemplates.size() > 1) {
					throw new IllegalStateException(
							"there is more than 1 RocketMQTemplates in Spring BeanFactory");
				}
				rocketMQTemplate = rocketMQTemplates.values().iterator().next();
			}
			else {
				rocketMQTemplate = new RocketMQTemplate();
				rocketMQTemplate.setObjectMapper(this.getApplicationContext()
						.getBeansOfType(ObjectMapper.class).values().iterator().next());
				DefaultMQProducer producer;
				String ak = mergedProperties.getAccessKey();
				String sk = mergedProperties.getSecretKey();
				if (!StringUtils.isEmpty(ak) && !StringUtils.isEmpty(sk)) {
					RPCHook rpcHook = new AclClientRPCHook(
							new SessionCredentials(ak, sk));
					producer = new DefaultMQProducer(producerGroup, rpcHook,
							mergedProperties.isEnableMsgTrace(),
							mergedProperties.getCustomizedTraceTopic());
					producer.setVipChannelEnabled(false);
					producer.setInstanceName(RocketMQUtil.getInstanceName(rpcHook,
							destination.getName() + "|" + UtilAll.getPid()));
				}
				else {
					producer = new DefaultMQProducer(producerGroup);
					producer.setVipChannelEnabled(
							producerProperties.getExtension().getVipChannelEnabled());
				}
				producer.setNamesrvAddr(RocketMQBinderUtils
						.getNameServerStr(mergedProperties.getNameServer()));
				producer.setSendMsgTimeout(
						producerProperties.getExtension().getSendMessageTimeout());
				producer.setRetryTimesWhenSendFailed(
						producerProperties.getExtension().getRetryTimesWhenSendFailed());
				producer.setRetryTimesWhenSendAsyncFailed(producerProperties
						.getExtension().getRetryTimesWhenSendAsyncFailed());
				producer.setCompressMsgBodyOverHowmuch(producerProperties.getExtension()
						.getCompressMessageBodyThreshold());
				producer.setRetryAnotherBrokerWhenNotStoreOK(
						producerProperties.getExtension().isRetryNextServer());
				producer.setMaxMessageSize(
						producerProperties.getExtension().getMaxMessageSize());
				if (!StringUtils.isEmpty(mergedProperties.getAccessChannel())) {
					producer.setAccessChannel(AccessChannel.valueOf(mergedProperties.getAccessChannel()));
				}
				rocketMQTemplate.setProducer(producer);
				if (producerProperties.isPartitioned()) {
					rocketMQTemplate
							.setMessageQueueSelector(new PartitionMessageQueueSelector());
				}
			}

			RocketMQMessageHandler messageHandler = new RocketMQMessageHandler(
					rocketMQTemplate, destination.getName(), producerGroup,
					producerProperties.getExtension().getTransactional(),
					instrumentationManager, producerProperties,
					((AbstractMessageChannel) channel).getInterceptors().stream().filter(
							channelInterceptor -> channelInterceptor instanceof MessageConverterConfigurer.PartitioningInterceptor)
							.map(channelInterceptor -> ((MessageConverterConfigurer.PartitioningInterceptor) channelInterceptor))
							.findFirst().orElse(null));
			messageHandler.setBeanFactory(this.getApplicationContext().getBeanFactory());
			messageHandler.setSync(producerProperties.getExtension().getSync());
			messageHandler.setHeaderMapper(createHeaderMapper(producerProperties));
			if (errorChannel != null) {
				messageHandler.setSendFailureChannel(errorChannel);
			}
			return messageHandler;
		}
		else {
			throw new RuntimeException("Binding for channel " + destination.getName()
					+ " has been disabled, message can't be delivered");
		}
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
			ExtendedConsumerProperties<RocketMQConsumerProperties> consumerProperties)
			throws Exception {
		if (group == null || "".equals(group)) {
			throw new RuntimeException(
					"'group must be configured for channel " + destination.getName());
		}

		RocketMQListenerBindingContainer listenerContainer = new RocketMQListenerBindingContainer(
				consumerProperties, rocketBinderConfigurationProperties, this);
		listenerContainer.setConsumerGroup(group);
		listenerContainer.setTopic(destination.getName());
		listenerContainer.setConsumeThreadMax(consumerProperties.getConcurrency());
		listenerContainer.setSuspendCurrentQueueTimeMillis(
				consumerProperties.getExtension().getSuspendCurrentQueueTimeMillis());
		listenerContainer.setDelayLevelWhenNextConsume(
				consumerProperties.getExtension().getDelayLevelWhenNextConsume());
		listenerContainer
				.setNameServer(rocketBinderConfigurationProperties.getNameServer());
		listenerContainer.setHeaderMapper(createHeaderMapper(consumerProperties));

		RocketMQInboundChannelAdapter rocketInboundChannelAdapter = new RocketMQInboundChannelAdapter(
				listenerContainer, consumerProperties, instrumentationManager);

		topicInUse.put(destination.getName(), group);

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
	protected PolledConsumerResources createPolledConsumerResources(String name,
			String group, ConsumerDestination destination,
			ExtendedConsumerProperties<RocketMQConsumerProperties> consumerProperties) {
		RocketMQMessageSource rocketMQMessageSource = new RocketMQMessageSource(
				rocketBinderConfigurationProperties, consumerProperties, name, group);
		return new PolledConsumerResources(rocketMQMessageSource,
				registerErrorInfrastructure(destination, group, consumerProperties,
						true));
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
					if (properties.getExtension().shouldRequeue()) {
						ack.acknowledge(Status.REQUEUE);
					}
					else {
						ack.acknowledge(Status.REJECT);
					}
				}
			}
		};
	}

	@Override
	public RocketMQConsumerProperties getExtendedConsumerProperties(String channelName) {
		return extendedBindingProperties.getExtendedConsumerProperties(channelName);
	}

	@Override
	public RocketMQProducerProperties getExtendedProducerProperties(String channelName) {
		return extendedBindingProperties.getExtendedProducerProperties(channelName);
	}

	public Map<String, String> getTopicInUse() {
		return topicInUse;
	}

	@Override
	public String getDefaultsPrefix() {
		return extendedBindingProperties.getDefaultsPrefix();
	}

	@Override
	public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
		return extendedBindingProperties.getExtendedPropertiesEntryClass();
	}

	public void setExtendedBindingProperties(
			RocketMQExtendedBindingProperties extendedBindingProperties) {
		this.extendedBindingProperties = extendedBindingProperties;
	}

	private RocketMQHeaderMapper createHeaderMapper(
			final ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties) {
		Set<String> trustedPackages = extendedConsumerProperties.getExtension()
				.getTrustedPackages();
		return createHeaderMapper(trustedPackages);
	}

	private RocketMQHeaderMapper createHeaderMapper(
			final ExtendedProducerProperties<RocketMQProducerProperties> producerProperties) {
		return createHeaderMapper(Collections.emptyList());
	}

	private RocketMQHeaderMapper createHeaderMapper(Collection<String> trustedPackages) {
		ObjectMapper objectMapper = this.getApplicationContext()
				.getBeansOfType(ObjectMapper.class).values().iterator().next();
		JacksonRocketMQHeaderMapper headerMapper = new JacksonRocketMQHeaderMapper(
				objectMapper);
		if (!StringUtils.isEmpty(trustedPackages)) {
			headerMapper.addTrustedPackages(trustedPackages);
		}
		return headerMapper;
	}

}
