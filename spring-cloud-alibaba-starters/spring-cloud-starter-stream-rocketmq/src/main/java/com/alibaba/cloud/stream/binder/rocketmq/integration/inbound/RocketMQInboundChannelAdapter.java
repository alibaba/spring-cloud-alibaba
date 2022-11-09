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

package com.alibaba.cloud.stream.binder.rocketmq.integration.inbound;

import java.util.List;
import java.util.function.Supplier;

import com.alibaba.cloud.stream.binder.rocketmq.metrics.Instrumentation;
import com.alibaba.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQMessageConverterSupport;
import com.alibaba.cloud.stream.binder.rocketmq.utils.RocketMQUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.integration.context.OrderlyShutdownCapable;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQInboundChannelAdapter extends MessageProducerSupport
		implements OrderlyShutdownCapable {

	private static final Logger log = LoggerFactory
			.getLogger(RocketMQInboundChannelAdapter.class);

	private RetryTemplate retryTemplate;

	private RecoveryCallback<Object> recoveryCallback;

	private DefaultMQPushConsumer pushConsumer;

	private final String topic;

	private final ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties;

	public RocketMQInboundChannelAdapter(String topic,
			ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties) {
		this.topic = topic;
		this.extendedConsumerProperties = extendedConsumerProperties;
	}

	@Override
	protected void onInit() {
		if (extendedConsumerProperties.getExtension() == null
				|| !extendedConsumerProperties.getExtension().getEnabled()) {
			return;
		}
		try {
			super.onInit();
			if (this.retryTemplate != null) {
				Assert.state(getErrorChannel() == null,
						"Cannot have an 'errorChannel' property when a 'RetryTemplate' is "
								+ "provided; use an 'ErrorMessageSendingRecoverer' in the 'recoveryCallback' property to "
								+ "send an error message when retries are exhausted");
				this.retryTemplate.registerListener(new RetryListener() {
					@Override
					public <T, E extends Throwable> boolean open(RetryContext context,
							RetryCallback<T, E> callback) {
						return true;
					}

					@Override
					public <T, E extends Throwable> void close(RetryContext context,
							RetryCallback<T, E> callback, Throwable throwable) {
					}

					@Override
					public <T, E extends Throwable> void onError(RetryContext context,
							RetryCallback<T, E> callback, Throwable throwable) {
					}
				});
			}
			pushConsumer = RocketMQConsumerFactory
					.initPushConsumer(extendedConsumerProperties);
			// prepare register consumer message listener,the next step is to be
			// compatible with a custom MessageListener.
			if (extendedConsumerProperties.getExtension().getPush().getOrderly()) {
				pushConsumer.registerMessageListener((MessageListenerOrderly) (msgs,
						context) -> RocketMQInboundChannelAdapter.this
								.consumeMessage(msgs, () -> {
									context.setSuspendCurrentQueueTimeMillis(
											extendedConsumerProperties.getExtension()
													.getPush()
													.getSuspendCurrentQueueTimeMillis());
									return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
								}, () -> ConsumeOrderlyStatus.SUCCESS));
			}
			else {
				pushConsumer.registerMessageListener((MessageListenerConcurrently) (msgs,
						context) -> RocketMQInboundChannelAdapter.this
								.consumeMessage(msgs, () -> {
									context.setDelayLevelWhenNextConsume(
											extendedConsumerProperties.getExtension()
													.getPush()
													.getDelayLevelWhenNextConsume());
									return ConsumeConcurrentlyStatus.RECONSUME_LATER;
								}, () -> ConsumeConcurrentlyStatus.CONSUME_SUCCESS));
			}
		}
		catch (Exception e) {
			log.error("DefaultMQPushConsumer init failed, Caused by " + e.getMessage());
			throw new MessagingException(MessageBuilder.withPayload(
					"DefaultMQPushConsumer init failed, Caused by " + e.getMessage())
					.build(), e);
		}
	}

	/**
	 * The actual execution of a user-defined input consumption service method.
	 * @param messageExtList rocket mq message list
	 * @param failSupplier {@link ConsumeConcurrentlyStatus} or
	 * {@link ConsumeOrderlyStatus}
	 * @param sucSupplier {@link ConsumeConcurrentlyStatus} or
	 * {@link ConsumeOrderlyStatus}
	 * @param <R> object
	 * @return R
	 */
	private <R> R consumeMessage(List<MessageExt> messageExtList,
			Supplier<R> failSupplier, Supplier<R> sucSupplier) {
		if (CollectionUtils.isEmpty(messageExtList)) {
			throw new MessagingException(
					"DefaultMQPushConsumer consuming failed, Caused by messageExtList is empty");
		}
		for (MessageExt messageExt : messageExtList) {
			try {
				Message<?> message = RocketMQMessageConverterSupport
						.convertMessage2Spring(messageExt);
				if (this.retryTemplate != null) {
					this.retryTemplate.execute(context -> {
						this.sendMessage(message);
						return message;
					}, this.recoveryCallback);
				}
				else {
					this.sendMessage(message);
				}
			}
			catch (Exception e) {
				log.warn("consume message failed. messageExt:{}", messageExt, e);
				return failSupplier.get();
			}
		}
		return sucSupplier.get();
	}

	@Override
	protected void doStart() {
		if (extendedConsumerProperties.getExtension() == null
				|| !extendedConsumerProperties.getExtension().getEnabled()) {
			return;
		}
		Instrumentation instrumentation = new Instrumentation(topic, this);
		try {
			pushConsumer.subscribe(topic, RocketMQUtils.getMessageSelector(
					extendedConsumerProperties.getExtension().getSubscription()));
			pushConsumer.start();
			instrumentation.markStartedSuccessfully();
		}
		catch (Exception e) {
			instrumentation.markStartFailed(e);
			log.error("DefaultMQPushConsumer init failed, Caused by " + e.getMessage());
			throw new MessagingException(MessageBuilder.withPayload(
					"DefaultMQPushConsumer init failed, Caused by " + e.getMessage())
					.build(), e);
		}
		finally {
			InstrumentationManager.addHealthInstrumentation(instrumentation);
		}
	}

	@Override
	protected void doStop() {
		if (pushConsumer != null) {
			pushConsumer.shutdown();
		}
	}

	public void setRetryTemplate(RetryTemplate retryTemplate) {
		this.retryTemplate = retryTemplate;
	}

	public void setRecoveryCallback(RecoveryCallback<Object> recoveryCallback) {
		this.recoveryCallback = recoveryCallback;
	}

	@Override
	public int beforeShutdown() {
		this.stop();
		return 0;
	}

	@Override
	public int afterShutdown() {
		return 0;
	}

}
