/*
 * Copyright (C) 2019 the original author or authors.
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
package org.springframework.cloud.alicloud.sms.endpoint;

import org.springframework.cloud.alicloud.sms.base.MessageListener;

import java.io.Serializable;

/**
 * @author pbting
 */
public class ReceiveMessageEntity implements Serializable {
	private String messageType;
	private String queueName;
	private MessageListener messageListener;

	public ReceiveMessageEntity(String messageType, String queueName,
			MessageListener messageListener) {
		this.messageType = messageType;
		this.queueName = queueName;
		this.messageListener = messageListener;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public MessageListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}
}