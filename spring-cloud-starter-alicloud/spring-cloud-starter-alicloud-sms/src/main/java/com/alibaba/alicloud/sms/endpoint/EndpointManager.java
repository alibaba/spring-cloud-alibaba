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

package com.alibaba.alicloud.sms.endpoint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import com.aliyuncs.dysmsapi.model.v20170525.SendBatchSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;

/**
 *
 */
public final class EndpointManager {

	private EndpointManager() {

	}

	private final static int BACKLOG_SIZE = 20;

	private final static ReentrantLock SEND_REENTRANT_LOCK = new ReentrantLock(true);

	private final static ReentrantLock SEND_BATCH_REENTRANT_LOCK = new ReentrantLock(
			true);

	private final static LinkedBlockingQueue<SendSmsRequest> SEND_SMS_REQUESTS = new LinkedBlockingQueue(
			BACKLOG_SIZE);

	private final static LinkedBlockingQueue<SendBatchSmsRequest> SEND_BATCH_SMS_REQUESTS = new LinkedBlockingQueue(
			BACKLOG_SIZE);

	private final static LinkedBlockingQueue<ReceiveMessageEntity> RECEIVE_MESSAGE_ENTITIES = new LinkedBlockingQueue(
			BACKLOG_SIZE);

	public static void addSendSmsRequest(SendSmsRequest sendSmsRequest) {
		if (SEND_SMS_REQUESTS.offer(sendSmsRequest)) {
			return;
		}
		try {
			SEND_REENTRANT_LOCK.lock();
			SEND_SMS_REQUESTS.poll();
			SEND_SMS_REQUESTS.offer(sendSmsRequest);
		}
		finally {
			SEND_REENTRANT_LOCK.unlock();
		}
	}

	public static void addSendBatchSmsRequest(SendBatchSmsRequest sendBatchSmsRequest) {
		if (SEND_BATCH_SMS_REQUESTS.offer(sendBatchSmsRequest)) {
			return;
		}
		try {
			SEND_BATCH_REENTRANT_LOCK.lock();
			SEND_BATCH_SMS_REQUESTS.poll();
			SEND_BATCH_SMS_REQUESTS.offer(sendBatchSmsRequest);
		}
		finally {
			SEND_BATCH_REENTRANT_LOCK.unlock();
		}
	}

	public static void addReceiveMessageEntity(
			ReceiveMessageEntity receiveMessageEntity) {
		if (RECEIVE_MESSAGE_ENTITIES.offer(receiveMessageEntity)) {
			return;
		}
		RECEIVE_MESSAGE_ENTITIES.poll();
		RECEIVE_MESSAGE_ENTITIES.offer(receiveMessageEntity);
	}

	public static Map<String, Object> getSmsEndpointMessage() {
		List<SendSmsRequest> sendSmsRequests = new LinkedList<>();
		List<SendBatchSmsRequest> sendBatchSmsRequests = new LinkedList<>();
		List<ReceiveMessageEntity> receiveMessageEntities = new LinkedList<>();
		try {
			SEND_REENTRANT_LOCK.lock();
			SEND_BATCH_REENTRANT_LOCK.lock();
			sendSmsRequests.addAll(SEND_SMS_REQUESTS);
			sendBatchSmsRequests.addAll(SEND_BATCH_SMS_REQUESTS);
		}
		finally {
			SEND_REENTRANT_LOCK.unlock();
			SEND_BATCH_REENTRANT_LOCK.unlock();
		}
		receiveMessageEntities.addAll(RECEIVE_MESSAGE_ENTITIES);

		Map<String, Object> endpointMessages = new HashMap<>();
		endpointMessages.put("send-sms-request", sendSmsRequests);
		endpointMessages.put("send-batch-sms-request", sendBatchSmsRequests);
		endpointMessages.put("message-listener", receiveMessageEntities);

		return endpointMessages;
	}

}
