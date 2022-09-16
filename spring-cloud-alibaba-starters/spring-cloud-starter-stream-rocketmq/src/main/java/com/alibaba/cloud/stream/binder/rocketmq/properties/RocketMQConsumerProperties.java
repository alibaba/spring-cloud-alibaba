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

package com.alibaba.cloud.stream.binder.rocketmq.properties;

import java.io.Serializable;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService;
import org.apache.rocketmq.client.impl.consumer.ConsumeMessageOrderlyService;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

/**
 * Extended consumer properties for RocketMQ binder.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQConsumerProperties extends RocketMQCommonProperties {

	/**
	 * Message model defines the way how messages are delivered to each consumer clients.
	 * This field defaults to clustering.
	 */
	private String messageModel = MessageModel.CLUSTERING.getModeCN();

	/**
	 * Queue allocation algorithm specifying how message queues are allocated to each
	 * consumer clients.
	 */
	private String allocateMessageQueueStrategy;

	/**
	 * The expressions include tags or SQL,as follow:
	 * <p>
	 * tag: {@code tag1||tag2||tag3 }; sql: {@code 'color'='blue' AND 'price'>100 } .
	 * </p>
	 * Determines whether there are specific characters "{@code ||}" in the expression to
	 * determine how the message is filtered,tags or SQL.
	 */
	private String subscription;

	/**
	 * Delay some time when exception occur .
	 */
	private long pullTimeDelayMillsWhenException = 1000;

	/**
	 * Consuming point on consumer booting.
	 *
	 * There are three consuming points:
	 * <ul>
	 * <li><code>CONSUME_FROM_LAST_OFFSET</code>: consumer clients pick up where it
	 * stopped previously. If it were a newly booting up consumer client, according aging
	 * of the consumer group, there are two cases:
	 * <ol>
	 * <li>if the consumer group is created so recently that the earliest message being
	 * subscribed has yet expired, which means the consumer group represents a lately
	 * launched business, consuming will start from the very beginning;</li>
	 * <li>if the earliest message being subscribed has expired, consuming will start from
	 * the latest messages, meaning messages born prior to the booting timestamp would be
	 * ignored.</li>
	 * </ol>
	 * </li>
	 * <li><code>CONSUME_FROM_FIRST_OFFSET</code>: Consumer client will start from
	 * earliest messages available.</li>
	 * <li><code>CONSUME_FROM_TIMESTAMP</code>: Consumer client will start from specified
	 * timestamp, which means messages born prior to {@link #consumeTimestamp} will be
	 * ignored</li>
	 * </ul>
	 */
	private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;

	/**
	 * Backtracking consumption time with second precision. Time format is
	 * 20131223171201<br>
	 * Implying Seventeen twelve and 01 seconds on December 23, 2013 year<br>
	 * Default backtracking consumption time Half an hour ago.
	 */
	private String consumeTimestamp = UtilAll
			.timeMillisToHumanString3(System.currentTimeMillis() - (1000 * 60 * 30));

	/**
	 * Flow control threshold on queue level, each message queue will cache at most 1000
	 * messages by default, Consider the {@link #pullBatchSize}, the instantaneous value
	 * may exceed the limit .
	 */
	private int pullThresholdForQueue = 1000;

	/**
	 * Limit the cached message size on queue level, each message queue will cache at most
	 * 100 MiB messages by default, Consider the {@link #pullBatchSize}, the instantaneous
	 * value may exceed the limit .
	 *
	 * <p>
	 * The size of a message only measured by message body, so it's not accurate
	 */
	private int pullThresholdSizeForQueue = 100;

	/**
	 * Maximum number of messages pulled each time.
	 */
	private int pullBatchSize = 10;

	/**
	 * Consume max span offset.it has no effect on sequential consumption.
	 */
	private int consumeMaxSpan = 2000;

	private Push push = new Push();

	private Pull pull = new Pull();

	public String getMessageModel() {
		return messageModel;
	}

	public RocketMQConsumerProperties setMessageModel(String messageModel) {
		this.messageModel = messageModel;
		return this;
	}

	public String getAllocateMessageQueueStrategy() {
		return allocateMessageQueueStrategy;
	}

	public void setAllocateMessageQueueStrategy(String allocateMessageQueueStrategy) {
		this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
	}

	public String getSubscription() {
		return subscription;
	}

	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}

	public Push getPush() {
		return push;
	}

	public void setPush(Push push) {
		this.push = push;
	}

	public long getPullTimeDelayMillsWhenException() {
		return pullTimeDelayMillsWhenException;
	}

	public RocketMQConsumerProperties setPullTimeDelayMillsWhenException(
			long pullTimeDelayMillsWhenException) {
		this.pullTimeDelayMillsWhenException = pullTimeDelayMillsWhenException;
		return this;
	}

	public ConsumeFromWhere getConsumeFromWhere() {
		return consumeFromWhere;
	}

	public RocketMQConsumerProperties setConsumeFromWhere(
			ConsumeFromWhere consumeFromWhere) {
		this.consumeFromWhere = consumeFromWhere;
		return this;
	}

	public String getConsumeTimestamp() {
		return consumeTimestamp;
	}

	public RocketMQConsumerProperties setConsumeTimestamp(String consumeTimestamp) {
		this.consumeTimestamp = consumeTimestamp;
		return this;
	}

	public int getPullThresholdForQueue() {
		return pullThresholdForQueue;
	}

	public RocketMQConsumerProperties setPullThresholdForQueue(
			int pullThresholdForQueue) {
		this.pullThresholdForQueue = pullThresholdForQueue;
		return this;
	}

	public int getPullThresholdSizeForQueue() {
		return pullThresholdSizeForQueue;
	}

	public RocketMQConsumerProperties setPullThresholdSizeForQueue(
			int pullThresholdSizeForQueue) {
		this.pullThresholdSizeForQueue = pullThresholdSizeForQueue;
		return this;
	}

	public int getPullBatchSize() {
		return pullBatchSize;
	}

	public RocketMQConsumerProperties setPullBatchSize(int pullBatchSize) {
		this.pullBatchSize = pullBatchSize;
		return this;
	}

	public Pull getPull() {
		return pull;
	}

	public RocketMQConsumerProperties setPull(Pull pull) {
		this.pull = pull;
		return this;
	}

	public int getConsumeMaxSpan() {
		return consumeMaxSpan;
	}

	public RocketMQConsumerProperties setConsumeMaxSpan(int consumeMaxSpan) {
		this.consumeMaxSpan = consumeMaxSpan;
		return this;
	}

	public static class Push implements Serializable {

		private static final long serialVersionUID = -7398468554978817630L;

		/**
		 * if orderly is true, using {@link MessageListenerOrderly} else if orderly if
		 * false, using {@link MessageListenerConcurrently}.
		 */
		private boolean orderly = false;

		/**
		 * Suspending pulling time for cases requiring slow pulling like flow-control
		 * scenario. see{@link ConsumeMessageOrderlyService#processConsumeResult}.
		 * see{@link ConsumeOrderlyContext#getSuspendCurrentQueueTimeMillis}.
		 */
		private int suspendCurrentQueueTimeMillis = 1000;

		/**
		 * https://github.com/alibaba/spring-cloud-alibaba/issues/1866 Max re-consume
		 * times. -1 means 16 times. If messages are re-consumed more than
		 * {@link #maxReconsumeTimes} before success, it's be directed to a deletion queue
		 * waiting.
		 */
		private int maxReconsumeTimes;

		/**
		 * for concurrently listener. message consume retry strategy. -1 means dlq(or
		 * discard. see {@link ConsumeMessageConcurrentlyService#processConsumeResult}.
		 * see {@link ConsumeConcurrentlyContext#getDelayLevelWhenNextConsume}.
		 */
		private int delayLevelWhenNextConsume = 0;

		/**
		 * Flow control threshold on topic level, default value is -1(Unlimited)
		 * <p>
		 * The value of {@code pullThresholdForQueue} will be overwrote and calculated
		 * based on {@code pullThresholdForTopic} if it is't unlimited
		 * <p>
		 * For example, if the value of pullThresholdForTopic is 1000 and 10 message
		 * queues are assigned to this consumer, then pullThresholdForQueue will be set to
		 * 100.
		 */
		private int pullThresholdForTopic = -1;

		/**
		 * Limit the cached message size on topic level, default value is -1
		 * MiB(Unlimited)
		 * <p>
		 * The value of {@code pullThresholdSizeForQueue} will be overwrote and calculated
		 * based on {@code pullThresholdSizeForTopic} if it is't unlimited .
		 * <p>
		 * For example, if the value of pullThresholdSizeForTopic is 1000 MiB and 10
		 * message queues are assigned to this consumer, then pullThresholdSizeForQueue
		 * will be set to 100 MiB .
		 */
		private int pullThresholdSizeForTopic = -1;

		/**
		 * Message pull Interval.
		 */
		private long pullInterval = 0;

		/**
		 * Batch consumption size.
		 */
		private int consumeMessageBatchMaxSize = 1;

		public boolean getOrderly() {
			return orderly;
		}

		public void setOrderly(boolean orderly) {
			this.orderly = orderly;
		}

		public int getSuspendCurrentQueueTimeMillis() {
			return suspendCurrentQueueTimeMillis;
		}

		public void setSuspendCurrentQueueTimeMillis(int suspendCurrentQueueTimeMillis) {
			this.suspendCurrentQueueTimeMillis = suspendCurrentQueueTimeMillis;
		}

		public int getMaxReconsumeTimes() {
			return maxReconsumeTimes;
		}

		public void setMaxReconsumeTimes(int maxReconsumeTimes) {
			this.maxReconsumeTimes = maxReconsumeTimes;
		}

		public int getDelayLevelWhenNextConsume() {
			return delayLevelWhenNextConsume;
		}

		public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
			this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
		}

		public int getPullThresholdForTopic() {
			return pullThresholdForTopic;
		}

		public void setPullThresholdForTopic(int pullThresholdForTopic) {
			this.pullThresholdForTopic = pullThresholdForTopic;
		}

		public int getPullThresholdSizeForTopic() {
			return pullThresholdSizeForTopic;
		}

		public void setPullThresholdSizeForTopic(int pullThresholdSizeForTopic) {
			this.pullThresholdSizeForTopic = pullThresholdSizeForTopic;
		}

		public long getPullInterval() {
			return pullInterval;
		}

		public void setPullInterval(long pullInterval) {
			this.pullInterval = pullInterval;
		}

		public int getConsumeMessageBatchMaxSize() {
			return consumeMessageBatchMaxSize;
		}

		public void setConsumeMessageBatchMaxSize(int consumeMessageBatchMaxSize) {
			this.consumeMessageBatchMaxSize = consumeMessageBatchMaxSize;
		}

	}

	public static class Pull implements Serializable {

		/**
		 * The poll timeout in milliseconds.
		 */
		private long pollTimeoutMillis = 1000 * 5;

		/**
		 * Pull thread number.
		 */
		private int pullThreadNums = 20;

		/**
		 * Interval time in in milliseconds for checking changes in topic metadata.
		 */
		private long topicMetadataCheckIntervalMillis = 30 * 1000;

		/**
		 * Long polling mode, the Consumer connection timeout(must greater than
		 * brokerSuspendMaxTimeMillis), it is not recommended to modify.
		 */
		private long consumerTimeoutMillisWhenSuspend = 1000 * 30;

		/**
		 * Ack state handling, including receive, reject, and retry, when a consumption
		 * exception occurs.
		 */
		private String errAcknowledge;

		private long pullThresholdForAll = 1000L;

		public long getPollTimeoutMillis() {
			return pollTimeoutMillis;
		}

		public void setPollTimeoutMillis(long pollTimeoutMillis) {
			this.pollTimeoutMillis = pollTimeoutMillis;
		}

		public int getPullThreadNums() {
			return pullThreadNums;
		}

		public void setPullThreadNums(int pullThreadNums) {
			this.pullThreadNums = pullThreadNums;
		}

		public long getTopicMetadataCheckIntervalMillis() {
			return topicMetadataCheckIntervalMillis;
		}

		public void setTopicMetadataCheckIntervalMillis(
				long topicMetadataCheckIntervalMillis) {
			this.topicMetadataCheckIntervalMillis = topicMetadataCheckIntervalMillis;
		}

		public long getConsumerTimeoutMillisWhenSuspend() {
			return consumerTimeoutMillisWhenSuspend;
		}

		public void setConsumerTimeoutMillisWhenSuspend(
				long consumerTimeoutMillisWhenSuspend) {
			this.consumerTimeoutMillisWhenSuspend = consumerTimeoutMillisWhenSuspend;
		}

		public String getErrAcknowledge() {
			return errAcknowledge;
		}

		public void setErrAcknowledge(String errAcknowledge) {
			this.errAcknowledge = errAcknowledge;
		}

		public long getPullThresholdForAll() {
			return pullThresholdForAll;
		}

		public void setPullThresholdForAll(long pullThresholdForAll) {
			this.pullThresholdForAll = pullThresholdForAll;
		}

	}

}
