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

package com.alibaba.cloud.stream.binder.rocketmq.provisioning.selector;

import java.util.List;

import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.stream.binder.BinderHeaders;

/**
 * @author wangxing
 */
public class PartitionMessageQueueSelector implements MessageQueueSelector {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PartitionMessageQueueSelector.class);

	@Override
	public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
		Integer partition = 0;
		try {
			partition = Math.abs(
					Integer.parseInt(msg.getProperty(BinderHeaders.PARTITION_HEADER)));
			if (partition >= mqs.size()) {
				LOGGER.warn(
						"the partition '{}' is greater than the number of queues '{}'.",
						partition, mqs.size());
				partition = partition % mqs.size();
			}
		}
		catch (NumberFormatException ignored) {
		}
		return mqs.get(partition);
	}

}
