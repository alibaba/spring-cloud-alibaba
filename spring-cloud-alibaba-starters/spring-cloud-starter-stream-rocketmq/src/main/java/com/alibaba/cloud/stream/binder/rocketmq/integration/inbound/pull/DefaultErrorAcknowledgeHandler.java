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

package com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.pull;

import com.alibaba.cloud.stream.binder.rocketmq.extend.ErrorAcknowledgeHandler;

import org.springframework.integration.acks.AcknowledgmentCallback.Status;
import org.springframework.messaging.Message;

/**
 * By default, if consumption fails, the corresponding MessageQueue will always be
 * retried, that is, the consumption of other messages in the MessageQueue will be
 * blocked.
 *
 * @author zkzlx
 */
public class DefaultErrorAcknowledgeHandler implements ErrorAcknowledgeHandler {

	/**
	 * Ack state handling, including receive, reject, and retry, when a consumption
	 * exception occurs.
	 * @param message message
	 * @return see {@link Status}
	 */
	@Override
	public Status handler(Message<?> message) {
		return Status.REQUEUE;
	}

}
