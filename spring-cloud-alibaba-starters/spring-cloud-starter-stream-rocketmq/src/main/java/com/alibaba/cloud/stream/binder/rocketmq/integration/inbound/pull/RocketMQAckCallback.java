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

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.util.Assert;

/**
 * A pollable {@link org.springframework.integration.core.MessageSource} for RocketMQ.
 *
 * @author zkzlx
 */
public class RocketMQAckCallback implements AcknowledgmentCallback {

	private final static Logger log = LoggerFactory.getLogger(RocketMQAckCallback.class);

	private boolean acknowledged;

	private boolean autoAckEnabled = true;

	private MessageExt messageExt;

	private DefaultLitePullConsumer consumer;

	private final MessageQueue messageQueue;

	public RocketMQAckCallback(DefaultLitePullConsumer consumer,
			MessageQueue messageQueue, MessageExt messageExt) {
		this.messageExt = messageExt;
		this.consumer = consumer;
		this.messageQueue = messageQueue;
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
		synchronized (messageQueue) {
			try {
				long offset = messageExt.getQueueOffset();
				switch (status) {
				case REJECT:
				case ACCEPT:
					consumer.committed(messageQueue);
					break;
				case REQUEUE:
					consumer.seek(messageQueue, offset);
					break;
				default:
					break;
				}
			}
			catch (MQClientException e) {
				throw new IllegalStateException(e);
			}
			finally {
				this.acknowledged = true;
			}
		}
	}

}
