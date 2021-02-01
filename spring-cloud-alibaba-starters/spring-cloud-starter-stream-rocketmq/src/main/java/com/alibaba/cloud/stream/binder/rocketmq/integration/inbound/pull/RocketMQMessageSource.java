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

package com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.pull;

import java.lang.reflect.Field;
import java.util.List;

import com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQConsumerFactory;
import com.alibaba.cloud.stream.binder.rocketmq.metrics.Instrumentation;
import com.alibaba.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQMessageConverterSupport;
import com.alibaba.cloud.stream.binder.rocketmq.utils.RocketMQUtils;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.AssignedMessageQueue;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.context.Lifecycle;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageSource extends AbstractMessageSource<Object>
		implements DisposableBean, Lifecycle {

	private final static Logger log = LoggerFactory
			.getLogger(RocketMQMessageSource.class);

	private DefaultLitePullConsumer consumer;
	private AssignedMessageQueue assignedMessageQueue;
	private volatile boolean running;

	private final String topic;
	private final MessageSelector messageSelector;
	private final ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties;

	public RocketMQMessageSource(String name,
			ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties) {
		this.topic = name;
		this.messageSelector = RocketMQUtils.getMessageSelector(
				extendedConsumerProperties.getExtension().getSubscription());
		this.extendedConsumerProperties = extendedConsumerProperties;

	}

	@Override
	public synchronized void start() {
		Instrumentation instrumentation = new Instrumentation(topic, this);
		try {
			if (this.isRunning()) {
				throw new IllegalStateException(
						"pull consumer already running. " + this.toString());
			}
			this.consumer = RocketMQConsumerFactory
					.initPullConsumer(extendedConsumerProperties);
			// This parameter must be 1, otherwise doReceive cannot be handled singly.
			this.consumer.setPullBatchSize(1);
			this.consumer.subscribe(topic, messageSelector);
			this.consumer.setAutoCommit(false);
			this.assignedMessageQueue = acquireAssignedMessageQueue(this.consumer);
			this.consumer.start();
			instrumentation.markStartedSuccessfully();
		}
		catch (MQClientException e) {
			instrumentation.markStartFailed(e);
			log.error("DefaultMQPullConsumer startup error: " + e.getMessage(), e);
		}
		finally {
			InstrumentationManager.addHealthInstrumentation(instrumentation);
		}
		this.running = true;
	}

	private AssignedMessageQueue acquireAssignedMessageQueue(
			DefaultLitePullConsumer consumer) {
		Field field = ReflectionUtils.findField(DefaultLitePullConsumer.class,
				"defaultLitePullConsumerImpl");
		assert field != null;
		field.setAccessible(true);
		DefaultLitePullConsumerImpl defaultLitePullConsumerImpl = (DefaultLitePullConsumerImpl) ReflectionUtils
				.getField(field, consumer);

		field = ReflectionUtils.findField(DefaultLitePullConsumerImpl.class,
				"assignedMessageQueue");
		assert field != null;
		field.setAccessible(true);
		return (AssignedMessageQueue) ReflectionUtils.getField(field,
				defaultLitePullConsumerImpl);
	}

	@Override
	public synchronized void stop() {
		if (this.isRunning() && null != consumer) {
			consumer.unsubscribe(topic);
			consumer.shutdown();
			this.running = false;
		}
	}

	@Override
	public synchronized boolean isRunning() {
		return running;
	}

	@Override
	protected synchronized Object doReceive() {
		List<MessageExt> messageExtList = consumer.poll();
		if (CollectionUtils.isEmpty(messageExtList) || messageExtList.size() > 1) {
			return null;
		}
		MessageExt messageExt = messageExtList.get(0);
		MessageQueue messageQueue = null;
		for (MessageQueue queue : assignedMessageQueue.getAssignedMessageQueues()) {
			if (queue.getQueueId() == messageExt.getQueueId()) {
				messageQueue = queue;
				break;
			}
		}
		Message message = RocketMQMessageConverterSupport
				.convertMessage2Spring(messageExtList.get(0));
		return MessageBuilder.fromMessage(message)
				.setHeader(IntegrationMessageHeaderAccessor.ACKNOWLEDGMENT_CALLBACK,
						new RocketMQAckCallback(this.consumer, assignedMessageQueue,
								messageQueue, messageExt))
				.build();
	}

	@Override
	public String getComponentType() {
		return "rocketmq:message-source";
	}

}
