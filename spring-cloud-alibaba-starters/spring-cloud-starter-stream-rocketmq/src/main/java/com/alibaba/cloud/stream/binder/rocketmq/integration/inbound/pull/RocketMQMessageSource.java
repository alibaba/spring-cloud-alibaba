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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQConsumerFactory;
import com.alibaba.cloud.stream.binder.rocketmq.metrics.Instrumentation;
import com.alibaba.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQMessageConverterSupport;
import com.alibaba.cloud.stream.binder.rocketmq.utils.RocketMQUtils;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.exception.MQClientException;
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

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageSource extends AbstractMessageSource<Object>
		implements DisposableBean, Lifecycle {

	private final static Logger log = LoggerFactory
			.getLogger(RocketMQMessageSource.class);

	private DefaultLitePullConsumer consumer;

	private final Map<String, Collection<MessageQueue>> messageQueuesForTopic = new ConcurrentHashMap<>();

	private volatile boolean running;

	private final String topic;

	private final MessageSelector messageSelector;

	private final ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties;

	private volatile Iterator<MessageExt> messageExtIterator = null;

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
			// this.consumer.setPullBatchSize(1);
			this.consumer.subscribe(topic, messageSelector);
			this.consumer.setAutoCommit(false);
			// register TopicMessageQueueChangeListener for messageQueuesForTopic
			consumer.registerTopicMessageQueueChangeListener(topic,
					messageQueuesForTopic::put);
			this.consumer.start();
			// Initialize messageQueuesForTopic immediately
			messageQueuesForTopic.put(topic, consumer.fetchMessageQueues(topic));
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

	private MessageQueue acquireCurrentMessageQueue(String topic, int queueId) {
		Collection<MessageQueue> messageQueueSet = messageQueuesForTopic.get(topic);
		if (CollectionUtils.isEmpty(messageQueueSet)) {
			return null;
		}
		for (MessageQueue messageQueue : messageQueueSet) {
			if (messageQueue.getQueueId() == queueId) {
				return messageQueue;
			}
		}
		return null;
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
		if (messageExtIterator == null) {
			List<MessageExt> messageExtList = consumer.poll();
			if (CollectionUtils.isEmpty(messageExtList)) {
				return null;
			}
			messageExtIterator = messageExtList.iterator();
		}
		MessageExt messageExt = messageExtIterator.next();
		if (!messageExtIterator.hasNext()) {
			messageExtIterator = null;
		}
		if (null == messageExt) {
			return null;
		}
		MessageQueue messageQueue = this.acquireCurrentMessageQueue(messageExt.getTopic(),
				messageExt.getQueueId());
		if (messageQueue == null) {
			throw new IllegalArgumentException(
					"The message queue is not in assigned list");
		}
		Message message = RocketMQMessageConverterSupport
				.convertMessage2Spring(messageExt);
		return MessageBuilder.fromMessage(message)
				.setHeader(IntegrationMessageHeaderAccessor.ACKNOWLEDGMENT_CALLBACK,
						new RocketMQAckCallback(this.consumer, messageQueue, messageExt))
				.build();
	}

	@Override
	public String getComponentType() {
		return "rocketmq:message-source";
	}

}
