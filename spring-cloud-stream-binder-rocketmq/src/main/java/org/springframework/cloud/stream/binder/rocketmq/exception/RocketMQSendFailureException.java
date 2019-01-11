/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.binder.rocketmq.exception;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

/**
 * An exception that is the payload of an {@code ErrorMessage} when occurs send failure.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @since 0.2.2
 */
public class RocketMQSendFailureException extends MessagingException {

	private final org.apache.rocketmq.common.message.Message rocketmqMsg;

	public RocketMQSendFailureException(Message<?> message,
			org.apache.rocketmq.common.message.Message rocketmqMsg, Throwable cause) {
		super(message, cause);
		this.rocketmqMsg = rocketmqMsg;
	}

	public org.apache.rocketmq.common.message.Message getRocketmqMsg() {
		return rocketmqMsg;
	}

	@Override
	public String toString() {
		return super.toString() + " [rocketmqMsg=" + this.rocketmqMsg + "]";
	}

}
