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

package com.alibaba.cloud.stream.binder.rocketmq.consuming;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.rocketmq.common.message.MessageQueue;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageQueueChooser {

	private volatile int queueIndex = 0;

	private volatile List<MessageQueue> messageQueues;

	public MessageQueue choose() {
		return messageQueues.get(queueIndex);
	}

	public int requeue() {
		if (queueIndex - 1 < 0) {
			this.queueIndex = messageQueues.size() - 1;
		}
		else {
			this.queueIndex = this.queueIndex - 1;
		}
		return this.queueIndex;
	}

	public void increment() {
		this.queueIndex = (this.queueIndex + 1) % messageQueues.size();
	}

	public void reset(Set<MessageQueue> queueSet) {
		this.messageQueues = null;
		this.messageQueues = new ArrayList<>(queueSet);
		this.queueIndex = 0;
	}

	public List<MessageQueue> getMessageQueues() {
		return messageQueues;
	}

}
