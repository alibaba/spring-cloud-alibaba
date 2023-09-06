/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.examples.orderly;

import java.util.List;

import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

/**
 * @author sorie
 */
@Component
public class OrderlyMessageQueueSelector implements MessageQueueSelector {

	private static final Logger log = LoggerFactory
			.getLogger(OrderlyMessageQueueSelector.class);

	/**
	 * to select a fixed queue by id.
	 * @param mqs all message queues of this topic.
	 * @param msg mq message.
	 * @param arg mq arguments.
	 * @return message queue selected.
	 */
	@Override
	public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
		Integer id = (Integer) ((MessageHeaders) arg)
				.get(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID);
		int index = id % RocketMQOrderlyConsumeApplication.tags.length % mqs.size();
		return mqs.get(index);
	}

}
