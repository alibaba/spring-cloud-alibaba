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
import org.springframework.messaging.MessagingException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

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

	private final String name;

	private final MessageSelector messageSelector;

	private final ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties;

	private volatile Iterator<MessageExt> messageExtIterator = null;

	public RocketMQMessageSource(String name,
			ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties) {
		this.name = name;
		this.messageSelector = RocketMQUtils.getMessageSelector(
				extendedConsumerProperties.getExtension().getSubscription());
		this.extendedConsumerProperties = extendedConsumerProperties;

	}

	@Override
	public synchronized void start() {
		Instrumentation instrumentation = new Instrumentation(name, this);
		try {
			if (this.isRunning()) {
				throw new IllegalStateException(
						"pull consumer already running. " + this.toString());
			}
			this.consumer = RocketMQConsumerFactory
					.initPullConsumer(name, extendedConsumerProperties);
			// This parameter must be 1, otherwise doReceive cannot be handled singly.
			// this.consumer.setPullBatchSize(1);
			String subscription = extendedConsumerProperties.getExtension().getSubscription();
			if (extendedConsumerProperties.isMultiplex()) {
				String[] topics = StringUtils.commaDelimitedListToStringArray(name);
				if (StringUtils.isEmpty(subscription)) {
					for (String topic : topics) {
						consumer.subscribe(topic, "*");
					}
				} else {
					if (subscription.contains(RocketMQUtils.SQL)) {
						throw new MessagingException("Multiplex scenario doesn't support SQL92 Filtering for now, please use Tag Filtering.");
					}
					String[] subscriptions = StringUtils.commaDelimitedListToStringArray(subscription);
					if (subscriptions.length != topics.length) {
						throw new MessagingException("Length of subscriptions should be the same as the length of topics.");
					}
					for (int i = 0; i < topics.length; i++) {
						consumer.subscribe(topics[i], subscriptions[i]);
					}
				}
				// Initialize messageQueuesForTopic immediately
				for (String topic: topics) {
					messageQueuesForTopic.put(topic, consumer.fetchMessageQueues(topic));
				}
			} else {
				consumer.subscribe(name, RocketMQUtils.getMessageSelector(subscription));
				messageQueuesForTopic.put(name, consumer.fetchMessageQueues(name));
			}

			this.consumer.setAutoCommit(false);
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

	private MessageQueue acquireCurrentMessageQueue(String topic, int queueId,
			String brokerName) {
		Collection<MessageQueue> messageQueueSet = messageQueuesForTopic.get(topic);
		if (CollectionUtils.isEmpty(messageQueueSet)) {
			return null;
		}
		for (MessageQueue messageQueue : messageQueueSet) {
			if (messageQueue.getQueueId() == queueId && ObjectUtils
					.nullSafeEquals(brokerName, messageQueue.getBrokerName())) {
				return messageQueue;
			}
		}
		return null;
	}

	@Override
	public synchronized void stop() {
		if (this.isRunning() && null != consumer) {
			for (String topic: StringUtils.commaDelimitedListToStringArray(name)) {
				consumer.unsubscribe(topic);
			}
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
				messageExt.getQueueId(), messageExt.getBrokerName());
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
