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

package org.springframework.cloud.stream.binder.rocketmq.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQMessageHeaderAccessor;
import org.springframework.cloud.stream.binder.rocketmq.consuming.Acknowledgement;
import org.springframework.cloud.stream.binder.rocketmq.consuming.ConsumersManager;
import org.springframework.cloud.stream.binder.rocketmq.metrics.ConsumerInstrumentation;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQInboundChannelAdapter extends MessageProducerSupport {

	private static final Logger logger = LoggerFactory
			.getLogger(RocketMQInboundChannelAdapter.class);

	private ConsumerInstrumentation consumerInstrumentation;

	private InstrumentationManager instrumentationManager;

	private RetryTemplate retryTemplate;

	private RecoveryCallback<? extends Object> recoveryCallback;

	private final ExtendedConsumerProperties<RocketMQConsumerProperties> consumerProperties;

	private final String destination;

	private final String group;

	private final ConsumersManager consumersManager;

	public RocketMQInboundChannelAdapter(ConsumersManager consumersManager,
			ExtendedConsumerProperties<RocketMQConsumerProperties> consumerProperties,
			String destination, String group,
			InstrumentationManager instrumentationManager) {
		this.consumersManager = consumersManager;
		this.consumerProperties = consumerProperties;
		this.destination = destination;
		this.group = group;
		this.instrumentationManager = instrumentationManager;
	}

	@Override
	protected void doStart() {
		if (consumerProperties == null
				|| !consumerProperties.getExtension().getEnabled()) {
			return;
		}

		String tags = consumerProperties.getExtension().getTags();
		Boolean isOrderly = consumerProperties.getExtension().getOrderly();

		DefaultMQPushConsumer consumer = consumersManager.getOrCreateConsumer(group,
				destination, consumerProperties);

		final CloudStreamMessageListener listener = isOrderly
				? new CloudStreamMessageListenerOrderly()
				: new CloudStreamMessageListenerConcurrently();

		if (retryTemplate != null) {
			retryTemplate.registerListener(listener);
		}

		Set<String> tagsSet = tags == null ? new HashSet<>()
				: Arrays.stream(tags.split("\\|\\|")).map(String::trim)
						.collect(Collectors.toSet());

		Optional.ofNullable(instrumentationManager).ifPresent(manager -> {
			consumerInstrumentation = manager.getConsumerInstrumentation(destination);
			manager.addHealthInstrumentation(consumerInstrumentation);
		});

		try {
			if (!StringUtils.isEmpty(consumerProperties.getExtension().getSql())) {
				consumer.subscribe(destination, MessageSelector
						.bySql(consumerProperties.getExtension().getSql()));
			}
			else {
				consumer.subscribe(destination, String.join(" || ", tagsSet));
			}
			Optional.ofNullable(consumerInstrumentation)
					.ifPresent(c -> c.markStartedSuccessfully());
		}
		catch (MQClientException e) {
			Optional.ofNullable(consumerInstrumentation)
					.ifPresent(c -> c.markStartFailed(e));
			logger.error("RocketMQ Consumer hasn't been subscribed. Caused by "
					+ e.getErrorMessage(), e);
			throw new RuntimeException("RocketMQ Consumer hasn't been subscribed.", e);
		}

		consumer.registerMessageListener(listener);

		try {
			consumersManager.startConsumer(group);
		}
		catch (MQClientException e) {
			logger.error(
					"RocketMQ Consumer startup failed. Caused by " + e.getErrorMessage(),
					e);
			throw new RuntimeException("RocketMQ Consumer startup failed.", e);
		}
	}

	@Override
	protected void doStop() {
		consumersManager.stopConsumer(group);
	}

	public void setRetryTemplate(RetryTemplate retryTemplate) {
		this.retryTemplate = retryTemplate;
	}

	public void setRecoveryCallback(RecoveryCallback<? extends Object> recoveryCallback) {
		this.recoveryCallback = recoveryCallback;
	}

	protected class CloudStreamMessageListener implements MessageListener, RetryListener {

		Acknowledgement consumeMessage(final List<MessageExt> msgs) {
			boolean enableRetry = RocketMQInboundChannelAdapter.this.retryTemplate != null;
			try {
				if (enableRetry) {
					return RocketMQInboundChannelAdapter.this.retryTemplate.execute(
							(RetryCallback<Acknowledgement, Exception>) context -> doSendMsgs(
									msgs, context),
							new RecoveryCallback<Acknowledgement>() {
								@Override
								public Acknowledgement recover(RetryContext context)
										throws Exception {
									RocketMQInboundChannelAdapter.this.recoveryCallback
											.recover(context);
									if (ClassUtils.isAssignable(this.getClass(),
											MessageListenerConcurrently.class)) {
										return Acknowledgement
												.buildConcurrentlyInstance();
									}
									else {
										return Acknowledgement.buildOrderlyInstance();
									}
								}
							});
				}
				else {
					Acknowledgement result = doSendMsgs(msgs, null);
					Optional.ofNullable(
							RocketMQInboundChannelAdapter.this.instrumentationManager)
							.ifPresent(manager -> {
								manager.getConsumerInstrumentation(
										RocketMQInboundChannelAdapter.this.destination)
										.markConsumed();
							});
					return result;
				}
			}
			catch (Exception e) {
				logger.error(
						"RocketMQ Message hasn't been processed successfully. Caused by ",
						e);
				Optional.ofNullable(
						RocketMQInboundChannelAdapter.this.instrumentationManager)
						.ifPresent(manager -> {
							manager.getConsumerInstrumentation(
									RocketMQInboundChannelAdapter.this.destination)
									.markConsumedFailure();
						});
				throw new RuntimeException(
						"RocketMQ Message hasn't been processed successfully. Caused by ",
						e);
			}
		}

		private Acknowledgement doSendMsgs(final List<MessageExt> msgs,
				RetryContext context) {
			List<Acknowledgement> acknowledgements = new ArrayList<>();
			msgs.forEach(msg -> {
				String retryInfo = context == null ? ""
						: "retryCount-" + String.valueOf(context.getRetryCount()) + "|";
				logger.debug(retryInfo + "consuming msg:\n" + msg);
				logger.debug(retryInfo + "message body:\n" + new String(msg.getBody()));
				Acknowledgement acknowledgement = new Acknowledgement();
				Message<byte[]> toChannel = MessageBuilder.withPayload(msg.getBody())
						.setHeaders(new RocketMQMessageHeaderAccessor()
								.withAcknowledgment(acknowledgement)
								.withTags(msg.getTags()).withKeys(msg.getKeys())
								.withFlag(msg.getFlag()).withRocketMessage(msg))
						.build();
				acknowledgements.add(acknowledgement);
				RocketMQInboundChannelAdapter.this.sendMessage(toChannel);
			});
			return acknowledgements.get(0);
		}

		@Override
		public <T, E extends Throwable> boolean open(RetryContext context,
				RetryCallback<T, E> callback) {
			return true;
		}

		@Override
		public <T, E extends Throwable> void close(RetryContext context,
				RetryCallback<T, E> callback, Throwable throwable) {
			if (throwable != null) {
				Optional.ofNullable(
						RocketMQInboundChannelAdapter.this.instrumentationManager)
						.ifPresent(manager -> {
							manager.getConsumerInstrumentation(
									RocketMQInboundChannelAdapter.this.destination)
									.markConsumedFailure();
						});
			}
			else {
				Optional.ofNullable(
						RocketMQInboundChannelAdapter.this.instrumentationManager)
						.ifPresent(manager -> {
							manager.getConsumerInstrumentation(
									RocketMQInboundChannelAdapter.this.destination)
									.markConsumed();
						});
			}
		}

		@Override
		public <T, E extends Throwable> void onError(RetryContext context,
				RetryCallback<T, E> callback, Throwable throwable) {
		}
	}

	protected class CloudStreamMessageListenerConcurrently
			extends CloudStreamMessageListener implements MessageListenerConcurrently {

		@Override
		public ConsumeConcurrentlyStatus consumeMessage(final List<MessageExt> msgs,
				ConsumeConcurrentlyContext context) {
			Acknowledgement acknowledgement = consumeMessage(msgs);
			context.setDelayLevelWhenNextConsume(
					acknowledgement.getConsumeConcurrentlyDelayLevel());
			return acknowledgement.getConsumeConcurrentlyStatus();
		}
	}

	protected class CloudStreamMessageListenerOrderly extends CloudStreamMessageListener
			implements MessageListenerOrderly {

		@Override
		public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs,
				ConsumeOrderlyContext context) {
			Acknowledgement acknowledgement = consumeMessage(msgs);
			context.setSuspendCurrentQueueTimeMillis(
					(acknowledgement.getConsumeOrderlySuspendCurrentQueueTimeMill()));
			return acknowledgement.getConsumeOrderlyStatus();
		}

	}

}
