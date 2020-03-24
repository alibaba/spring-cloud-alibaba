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

package com.alibaba.cloud.stream.binder.rocketmq.integration;

import java.util.List;
import java.util.Set;

import com.alibaba.cloud.stream.binder.rocketmq.RocketMQBinderUtils;
import com.alibaba.cloud.stream.binder.rocketmq.consuming.RocketMQMessageQueueChooser;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.MessageQueueListener;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.context.Lifecycle;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.integration.acks.AcknowledgmentCallbackFactory;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageSource extends AbstractMessageSource<Object>
		implements DisposableBean, Lifecycle {

	private final static Logger log = LoggerFactory
			.getLogger(RocketMQMessageSource.class);

	private final RocketMQCallbackFactory ackCallbackFactory;

	private final RocketMQBinderConfigurationProperties rocketMQBinderConfigurationProperties;

	private final ExtendedConsumerProperties<RocketMQConsumerProperties> rocketMQConsumerProperties;

	private final String topic;

	private final String group;

	private final Object consumerMonitor = new Object();

	private DefaultMQPullConsumer consumer;

	private boolean running;

	private MessageSelector messageSelector;

	private RocketMQMessageQueueChooser messageQueueChooser = new RocketMQMessageQueueChooser();

	public RocketMQMessageSource(
			RocketMQBinderConfigurationProperties rocketMQBinderConfigurationProperties,
			ExtendedConsumerProperties<RocketMQConsumerProperties> rocketMQConsumerProperties,
			String topic, String group) {
		this(new RocketMQCallbackFactory(), rocketMQBinderConfigurationProperties,
				rocketMQConsumerProperties, topic, group);
	}

	public RocketMQMessageSource(RocketMQCallbackFactory ackCallbackFactory,
			RocketMQBinderConfigurationProperties rocketMQBinderConfigurationProperties,
			ExtendedConsumerProperties<RocketMQConsumerProperties> rocketMQConsumerProperties,
			String topic, String group) {
		this.ackCallbackFactory = ackCallbackFactory;
		this.rocketMQBinderConfigurationProperties = rocketMQBinderConfigurationProperties;
		this.rocketMQConsumerProperties = rocketMQConsumerProperties;
		this.topic = topic;
		this.group = group;
	}

	@Override
	public synchronized void start() {
		if (this.isRunning()) {
			throw new IllegalStateException(
					"pull consumer already running. " + this.toString());
		}
		try {
			consumer = new DefaultMQPullConsumer(group);
			consumer.setNamesrvAddr(RocketMQBinderUtils.getNameServerStr(
					rocketMQBinderConfigurationProperties.getNameServer()));
			consumer.setConsumerPullTimeoutMillis(
					rocketMQConsumerProperties.getExtension().getPullTimeout());
			consumer.setMessageModel(MessageModel.CLUSTERING);

			String tags = rocketMQConsumerProperties.getExtension().getTags();
			String sql = rocketMQConsumerProperties.getExtension().getSql();

			if (!StringUtils.isEmpty(tags) && !StringUtils.isEmpty(sql)) {
				messageSelector = MessageSelector.byTag(tags);
			}
			else if (!StringUtils.isEmpty(tags)) {
				messageSelector = MessageSelector.byTag(tags);
			}
			else if (!StringUtils.isEmpty(sql)) {
				messageSelector = MessageSelector.bySql(sql);
			}

			consumer.registerMessageQueueListener(topic, new MessageQueueListener() {
				@Override
				public void messageQueueChanged(String topic, Set<MessageQueue> mqAll,
						Set<MessageQueue> mqDivided) {
					log.info(
							"messageQueueChanged, topic='{}', mqAll=`{}`, mqDivided=`{}`",
							topic, mqAll, mqDivided);
					switch (consumer.getMessageModel()) {
					case BROADCASTING:
						RocketMQMessageSource.this.resetMessageQueues(mqAll);
						break;
					case CLUSTERING:
						RocketMQMessageSource.this.resetMessageQueues(mqDivided);
						break;
					default:
						break;
					}
				}
			});
			consumer.start();
		}
		catch (MQClientException e) {
			log.error("DefaultMQPullConsumer startup error: " + e.getMessage(), e);
		}
		this.setRunning(true);
	}

	@Override
	public synchronized void stop() {
		if (this.isRunning()) {
			this.setRunning(false);
			consumer.shutdown();
		}
	}

	@Override
	public synchronized boolean isRunning() {
		return running;
	}

	@Override
	protected synchronized Object doReceive() {
		if (messageQueueChooser.getMessageQueues() == null
				|| messageQueueChooser.getMessageQueues().size() == 0) {
			return null;
		}
		try {
			int count = 0;
			while (count < messageQueueChooser.getMessageQueues().size()) {
				MessageQueue messageQueue;
				synchronized (this.consumerMonitor) {
					messageQueue = messageQueueChooser.choose();
					messageQueueChooser.increment();
				}

				long offset = consumer.fetchConsumeOffset(messageQueue,
						rocketMQConsumerProperties.getExtension().isFromStore());

				log.debug("topic='{}', group='{}', messageQueue='{}', offset now='{}'",
						this.topic, this.group, messageQueue, offset);

				PullResult pullResult;
				if (messageSelector != null) {
					pullResult = consumer.pull(messageQueue, messageSelector, offset, 1);
				}
				else {
					pullResult = consumer.pull(messageQueue, (String) null, offset, 1);
				}

				if (pullResult.getPullStatus() == PullStatus.FOUND) {
					List<MessageExt> messageExtList = pullResult.getMsgFoundList();

					Message message = RocketMQUtil
							.convertToSpringMessage(messageExtList.get(0));

					AcknowledgmentCallback ackCallback = this.ackCallbackFactory
							.createCallback(new RocketMQAckInfo(messageQueue, pullResult,
									consumer, offset));

					Message messageResult = MessageBuilder.fromMessage(message).setHeader(
							IntegrationMessageHeaderAccessor.ACKNOWLEDGMENT_CALLBACK,
							ackCallback).build();
					return messageResult;
				}
				else {
					log.debug("messageQueue='{}' PullResult='{}' with topic `{}`",
							messageQueueChooser.getMessageQueues(),
							pullResult.getPullStatus(), topic);
				}
				count++;
			}
		}
		catch (Exception e) {
			log.error("Consumer pull error: " + e.getMessage(), e);
		}
		return null;
	}

	@Override
	public String getComponentType() {
		return "rocketmq:message-source";
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}

	public synchronized void resetMessageQueues(Set<MessageQueue> queueSet) {
		log.info("resetMessageQueues, topic='{}', messageQueue=`{}`", topic, queueSet);
		synchronized (this.consumerMonitor) {
			this.messageQueueChooser.reset(queueSet);
		}
	}

	public static class RocketMQCallbackFactory
			implements AcknowledgmentCallbackFactory<RocketMQAckInfo> {

		@Override
		public AcknowledgmentCallback createCallback(RocketMQAckInfo info) {
			return new RocketMQAckCallback(info);
		}

	}

	public static class RocketMQAckCallback implements AcknowledgmentCallback {

		private final RocketMQAckInfo ackInfo;

		private boolean acknowledged;

		private boolean autoAckEnabled = true;

		public RocketMQAckCallback(RocketMQAckInfo ackInfo) {
			this.ackInfo = ackInfo;
		}

		protected void setAcknowledged(boolean acknowledged) {
			this.acknowledged = acknowledged;
		}

		@Override
		public boolean isAcknowledged() {
			return this.acknowledged;
		}

		@Override
		public void noAutoAck() {
			this.autoAckEnabled = false;
		}

		@Override
		public boolean isAutoAck() {
			return this.autoAckEnabled;
		}

		@Override
		public void acknowledge(Status status) {
			Assert.notNull(status, "'status' cannot be null");
			if (this.acknowledged) {
				throw new IllegalStateException("Already acknowledged");
			}
			log.debug("acknowledge(" + status.name() + ") for " + this);
			synchronized (this.ackInfo.getConsumerMonitor()) {
				try {
					switch (status) {
					case ACCEPT:
					case REJECT:
						ackInfo.getConsumer().updateConsumeOffset(
								ackInfo.getMessageQueue(),
								ackInfo.getPullResult().getNextBeginOffset());
						log.debug("messageQueue='{}' offset update to `{}`",
								ackInfo.getMessageQueue(), String.valueOf(
										ackInfo.getPullResult().getNextBeginOffset()));
						break;
					case REQUEUE:
						// decrease index and update offset of messageQueue of ackInfo
						int oldIndex = ackInfo.getMessageQueueChooser().requeue();
						ackInfo.getConsumer().updateConsumeOffset(
								ackInfo.getMessageQueue(), ackInfo.getOldOffset());
						log.debug(
								"messageQueue='{}' offset requeue to index:`{}`, oldOffset:'{}'",
								ackInfo.getMessageQueue(), oldIndex,
								ackInfo.getOldOffset());
						break;
					default:
						break;
					}
				}
				catch (MQClientException e) {
					log.error("acknowledge error: " + e.getErrorMessage(), e);
				}
				finally {
					this.acknowledged = true;
				}
			}
		}

		@Override
		public String toString() {
			return "RocketMQAckCallback{" + "ackInfo=" + ackInfo + ", acknowledged="
					+ acknowledged + ", autoAckEnabled=" + autoAckEnabled + '}';
		}

	}

	public class RocketMQAckInfo {

		private final MessageQueue messageQueue;

		private final PullResult pullResult;

		private final DefaultMQPullConsumer consumer;

		private final long oldOffset;

		public RocketMQAckInfo(MessageQueue messageQueue, PullResult pullResult,
				DefaultMQPullConsumer consumer, long oldOffset) {
			this.messageQueue = messageQueue;
			this.pullResult = pullResult;
			this.consumer = consumer;
			this.oldOffset = oldOffset;
		}

		public MessageQueue getMessageQueue() {
			return messageQueue;
		}

		public PullResult getPullResult() {
			return pullResult;
		}

		public DefaultMQPullConsumer getConsumer() {
			return consumer;
		}

		public RocketMQMessageQueueChooser getMessageQueueChooser() {
			return RocketMQMessageSource.this.messageQueueChooser;
		}

		public long getOldOffset() {
			return oldOffset;
		}

		public Object getConsumerMonitor() {
			return RocketMQMessageSource.this.consumerMonitor;
		}

		@Override
		public String toString() {
			return "RocketMQAckInfo{" + "messageQueue=" + messageQueue + ", pullResult="
					+ pullResult + ", consumer=" + consumer + ", oldOffset=" + oldOffset
					+ '}';
		}

	}

}
