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

package com.alibaba.cloud.stream.binder.rocketmq.integration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binding.MessageConverterConfigurer;
import org.springframework.context.Lifecycle;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.support.DefaultErrorMessageStrategy;
import org.springframework.integration.support.ErrorMessageStrategy;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.alibaba.cloud.stream.binder.rocketmq.RocketMQBinderConstants;
import com.alibaba.cloud.stream.binder.rocketmq.metrics.Instrumentation;
import com.alibaba.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQHeaderMapper;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageHandler extends AbstractMessageHandler implements Lifecycle {

	private final static Logger log = LoggerFactory
			.getLogger(RocketMQMessageHandler.class);

	private ErrorMessageStrategy errorMessageStrategy = new DefaultErrorMessageStrategy();

	private MessageChannel sendFailureChannel;

	private final RocketMQTemplate rocketMQTemplate;

	private RocketMQHeaderMapper headerMapper;

	private final Boolean transactional;

	private final String destination;

	private final String groupName;

	private final InstrumentationManager instrumentationManager;

	private boolean sync = false;

	private volatile boolean running = false;

	private ExtendedProducerProperties<RocketMQProducerProperties> producerProperties;

	private MessageConverterConfigurer.PartitioningInterceptor partitioningInterceptor;

	public RocketMQMessageHandler(RocketMQTemplate rocketMQTemplate, String destination,
			String groupName, Boolean transactional,
			InstrumentationManager instrumentationManager,
			ExtendedProducerProperties<RocketMQProducerProperties> producerProperties,
			MessageConverterConfigurer.PartitioningInterceptor partitioningInterceptor) {
		this.rocketMQTemplate = rocketMQTemplate;
		this.destination = destination;
		this.groupName = groupName;
		this.transactional = transactional;
		this.instrumentationManager = instrumentationManager;
		this.producerProperties = producerProperties;
		this.partitioningInterceptor = partitioningInterceptor;
	}

	@Override
	public void start() {
		if (!transactional) {
			instrumentationManager
					.addHealthInstrumentation(new Instrumentation(destination));
			try {
				rocketMQTemplate.afterPropertiesSet();
				instrumentationManager.getHealthInstrumentation(destination)
						.markStartedSuccessfully();
			}
			catch (Exception e) {
				instrumentationManager.getHealthInstrumentation(destination)
						.markStartFailed(e);
				log.error("RocketMQTemplate startup failed, Caused by " + e.getMessage());
				throw new MessagingException(MessageBuilder.withPayload(
						"RocketMQTemplate startup failed, Caused by " + e.getMessage())
						.build(), e);
			}
		}
		if (producerProperties.isPartitioned()) {
			try {
				List<MessageQueue> messageQueues = rocketMQTemplate.getProducer()
						.fetchPublishMessageQueues(destination);
				if (producerProperties.getPartitionCount() != messageQueues.size()) {
					logger.info(String.format(
							"The partition count of topic '%s' will change from '%s' to '%s'",
							destination, producerProperties.getPartitionCount(),
							messageQueues.size()));
					producerProperties.setPartitionCount(messageQueues.size());
					partitioningInterceptor
							.setPartitionCount(producerProperties.getPartitionCount());
				}
			}
			catch (MQClientException e) {
				logger.error("fetch publish message queues fail", e);
			}
		}
		running = true;
	}

	@Override
	public void stop() {
		if (!transactional) {
			rocketMQTemplate.destroy();
		}
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	protected void handleMessageInternal(
			org.springframework.messaging.Message<?> message) {
		try {
			// issue 737 fix
			Map<String, String> jsonHeaders = headerMapper
					.fromHeaders(message.getHeaders());
			message = org.springframework.messaging.support.MessageBuilder
					.fromMessage(message).copyHeaders(jsonHeaders).build();

			final StringBuilder topicWithTags = new StringBuilder(destination);
			String tags = Optional
					.ofNullable(message.getHeaders().get(RocketMQHeaders.TAGS)).orElse("")
					.toString();
			if (!StringUtils.isEmpty(tags)) {
				topicWithTags.append(":").append(tags);
			}

			SendResult sendRes = null;
			if (transactional) {
				sendRes = rocketMQTemplate.sendMessageInTransaction(groupName,
						topicWithTags.toString(), message, message.getHeaders()
								.get(RocketMQBinderConstants.ROCKET_TRANSACTIONAL_ARG));
				log.debug("transactional send to topic " + topicWithTags + " " + sendRes);
			}
			else {
				int delayLevel = 0;
				try {
					Object delayLevelObj = message.getHeaders()
							.getOrDefault(MessageConst.PROPERTY_DELAY_TIME_LEVEL, 0);
					if (delayLevelObj instanceof Number) {
						delayLevel = ((Number) delayLevelObj).intValue();
					}
					else if (delayLevelObj instanceof String) {
						delayLevel = Integer.parseInt((String) delayLevelObj);
					}
				}
				catch (Exception e) {
					// ignore
				}
				boolean needSelectQueue = message.getHeaders()
						.containsKey(BinderHeaders.PARTITION_HEADER);
				if (sync) {
					if (needSelectQueue) {
						sendRes = rocketMQTemplate.syncSendOrderly(
								topicWithTags.toString(), message, "",
								rocketMQTemplate.getProducer().getSendMsgTimeout());
					}
					else {
						sendRes = rocketMQTemplate.syncSend(topicWithTags.toString(),
								message,
								rocketMQTemplate.getProducer().getSendMsgTimeout(),
								delayLevel);
					}
					log.debug("sync send to topic " + topicWithTags + " " + sendRes);
				}
				else {
					Message<?> finalMessage = message;
					SendCallback sendCallback = new SendCallback() {
						@Override
						public void onSuccess(SendResult sendResult) {
							log.debug("async send to topic " + topicWithTags + " "
									+ sendResult);
						}

						@Override
						public void onException(Throwable e) {
							log.error("RocketMQ Message hasn't been sent. Caused by "
									+ e.getMessage());
							if (getSendFailureChannel() != null) {
								getSendFailureChannel().send(
										RocketMQMessageHandler.this.errorMessageStrategy
												.buildErrorMessage(new MessagingException(
														finalMessage, e), null));
							}
						}
					};
					if (needSelectQueue) {
						rocketMQTemplate.asyncSendOrderly(topicWithTags.toString(),
								message, "", sendCallback,
								rocketMQTemplate.getProducer().getSendMsgTimeout());
					}
					else {
						rocketMQTemplate.asyncSend(topicWithTags.toString(), message,
								sendCallback);
					}
				}
			}
			if (sendRes != null && !sendRes.getSendStatus().equals(SendStatus.SEND_OK)) {
				if (getSendFailureChannel() != null) {
					this.getSendFailureChannel().send(message);
				}
				else {
					throw new MessagingException(message,
							new MQClientException("message hasn't been sent", null));
				}
			}
		}
		catch (Exception e) {
			log.error("RocketMQ Message hasn't been sent. Caused by " + e.getMessage());
			if (getSendFailureChannel() != null) {
				getSendFailureChannel().send(this.errorMessageStrategy
						.buildErrorMessage(new MessagingException(message, e), null));
			}
			else {
				throw new MessagingException(message, e);
			}
		}

	}

	/**
	 * Set the failure channel. After a send failure, an {@link ErrorMessage} will be sent
	 * to this channel with a payload of a {@link MessagingException} with the failed
	 * message and cause.
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

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	public RocketMQHeaderMapper getHeaderMapper() {
		return headerMapper;
	}

	public void setHeaderMapper(RocketMQHeaderMapper headerMapper) {
		this.headerMapper = headerMapper;
	}
}