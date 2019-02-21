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

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.LocalTransactionExecuter;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionCheckListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQMessageHeaderAccessor;
import org.springframework.cloud.stream.binder.rocketmq.exception.RocketMQSendFailureException;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.cloud.stream.binder.rocketmq.metrics.ProducerInstrumentation;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import org.springframework.context.Lifecycle;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.support.DefaultErrorMessageStrategy;
import org.springframework.integration.support.ErrorMessageStrategy;
import org.springframework.integration.support.MutableMessage;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageHandler extends AbstractMessageHandler implements Lifecycle {

	private ErrorMessageStrategy errorMessageStrategy = new DefaultErrorMessageStrategy();

	private DefaultMQProducer producer;

	private ProducerInstrumentation producerInstrumentation;

	private InstrumentationManager instrumentationManager;

	private LocalTransactionExecuter localTransactionExecuter;

	private TransactionCheckListener transactionCheckListener;

	private MessageChannel sendFailureChannel;

	private final ExtendedProducerProperties<RocketMQProducerProperties> producerProperties;

	private final String destination;

	private final RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties;

	private volatile boolean running = false;

	public RocketMQMessageHandler(String destination,
			ExtendedProducerProperties<RocketMQProducerProperties> producerProperties,
			RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties,
			InstrumentationManager instrumentationManager) {
		this.destination = destination;
		this.producerProperties = producerProperties;
		this.rocketBinderConfigurationProperties = rocketBinderConfigurationProperties;
		this.instrumentationManager = instrumentationManager;
	}

	@Override
	public void start() {
		if (producerProperties.getExtension().getTransactional()) {
			producer = new TransactionMQProducer(destination);
			if (transactionCheckListener != null) {
				((TransactionMQProducer) producer)
						.setTransactionCheckListener(transactionCheckListener);
			}
		}
		else {
			producer = new DefaultMQProducer(destination);
		}

		producer.setVipChannelEnabled(
				producerProperties.getExtension().getVipChannelEnabled());

		Optional.ofNullable(instrumentationManager).ifPresent(manager -> {
			producerInstrumentation = manager.getProducerInstrumentation(destination);
			manager.addHealthInstrumentation(producerInstrumentation);
		});

		producer.setNamesrvAddr(rocketBinderConfigurationProperties.getNamesrvAddr());

		if (producerProperties.getExtension().getMaxMessageSize() > 0) {
			producer.setMaxMessageSize(
					producerProperties.getExtension().getMaxMessageSize());
		}

		try {
			producer.start();
			Optional.ofNullable(producerInstrumentation)
					.ifPresent(p -> p.markStartedSuccessfully());
		}
		catch (MQClientException e) {
			Optional.ofNullable(producerInstrumentation)
					.ifPresent(p -> p.markStartFailed(e));
			logger.error(
					"RocketMQ Message hasn't been sent. Caused by " + e.getMessage());
			throw new MessagingException(e.getMessage(), e);
		}
		running = true;
	}

	@Override
	public void stop() {
		if (producer != null) {
			producer.shutdown();
		}
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	protected void handleMessageInternal(org.springframework.messaging.Message<?> message)
			throws Exception {
		Message toSend = null;
		try {
			if (message.getPayload() instanceof byte[]) {
				toSend = new Message(destination, (byte[]) message.getPayload());
			}
			else if (message.getPayload() instanceof String) {
				toSend = new Message(destination,
						((String) message.getPayload()).getBytes());
			}
			else {
				throw new UnsupportedOperationException("Payload class isn't supported: "
						+ message.getPayload().getClass());
			}
			RocketMQMessageHeaderAccessor headerAccessor = new RocketMQMessageHeaderAccessor(
					message);
			headerAccessor.setLeaveMutable(true);
			toSend.setDelayTimeLevel(headerAccessor.getDelayTimeLevel());
			toSend.setTags(headerAccessor.getTags());
			toSend.setKeys(headerAccessor.getKeys());
			toSend.setFlag(headerAccessor.getFlag());
			for (Map.Entry<String, String> entry : headerAccessor.getUserProperties()
					.entrySet()) {
				toSend.putUserProperty(entry.getKey(), entry.getValue());
			}

			SendResult sendRes;
			if (producerProperties.getExtension().getTransactional()) {
				sendRes = producer.sendMessageInTransaction(toSend,
						localTransactionExecuter, headerAccessor.getTransactionalArg());
			}
			else {
				sendRes = producer.send(toSend);
			}

			if (!sendRes.getSendStatus().equals(SendStatus.SEND_OK)) {
				if (getSendFailureChannel() != null) {
					this.getSendFailureChannel().send(message);
				}
				else {
					throw new RocketMQSendFailureException(message, toSend,
							new MQClientException("message hasn't been sent", null));
				}
			}
			if (message instanceof MutableMessage) {
				RocketMQMessageHeaderAccessor.putSendResult((MutableMessage) message,
						sendRes);
			}
			Optional.ofNullable(instrumentationManager).ifPresent(manager -> {
				manager.getRuntime().put(RocketMQBinderConstants.LASTSEND_TIMESTAMP,
						Instant.now().toEpochMilli());
			});
			Optional.ofNullable(producerInstrumentation).ifPresent(p -> p.markSent());
		}
		catch (MQClientException | RemotingException | MQBrokerException
				| InterruptedException | UnsupportedOperationException e) {
			Optional.ofNullable(producerInstrumentation)
					.ifPresent(p -> p.markSentFailure());
			logger.error(
					"RocketMQ Message hasn't been sent. Caused by " + e.getMessage());
			if (getSendFailureChannel() != null) {
				getSendFailureChannel().send(this.errorMessageStrategy.buildErrorMessage(
						new RocketMQSendFailureException(message, toSend, e), null));
			}
			else {
				throw new RocketMQSendFailureException(message, toSend, e);
			}
		}

	}

	/**
	 * Using in RocketMQ Transactional Mode. Set RocketMQ localTransactionExecuter in
	 * {@link DefaultMQProducer#sendMessageInTransaction}.
	 * @param localTransactionExecuter the executer running when produce msg.
	 */
	public void setLocalTransactionExecuter(
			LocalTransactionExecuter localTransactionExecuter) {
		this.localTransactionExecuter = localTransactionExecuter;
	}

	/**
	 * Using in RocketMQ Transactional Mode. Set RocketMQ transactionCheckListener in
	 * {@link TransactionMQProducer#setTransactionCheckListener}.
	 * @param transactionCheckListener the listener set in {@link TransactionMQProducer}.
	 */
	public void setTransactionCheckListener(
			TransactionCheckListener transactionCheckListener) {
		this.transactionCheckListener = transactionCheckListener;
	}

	/**
	 * Set the failure channel. After a send failure, an {@link ErrorMessage} will be sent
	 * to this channel with a payload of a {@link RocketMQSendFailureException} with the
	 * failed message and cause.
	 * @param sendFailureChannel the failure channel.
	 * @since 0.2.2
	 */
	public void setSendFailureChannel(MessageChannel sendFailureChannel) {
		this.sendFailureChannel = sendFailureChannel;
	}

	/**
	 * Set the error message strategy implementation to use when sending error messages
	 * after send failures. Cannot be null.
	 * @param errorMessageStrategy the implementation.
	 * @since 0.2.2
	 */
	public void setErrorMessageStrategy(ErrorMessageStrategy errorMessageStrategy) {
		Assert.notNull(errorMessageStrategy, "'errorMessageStrategy' cannot be null");
		this.errorMessageStrategy = errorMessageStrategy;
	}

	public MessageChannel getSendFailureChannel() {
		return sendFailureChannel;
	}
}