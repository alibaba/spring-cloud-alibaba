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

package com.alibaba.cloud.examples.tx;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Component("myTransactionListener")
public class TransactionListenerImpl implements TransactionListener {

	/**
	 * Execute local transaction.
	 * @param msg messages
	 * @param arg message args
	 * @return Transaction state
	 */
	@Override
	public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
		Object num = msg.getProperty("test");

		if ("1".equals(num)) {
			System.out.println("executer: " + new String(msg.getBody()) + " unknown");
			return LocalTransactionState.UNKNOW;
		}
		else if ("2".equals(num)) {
			System.out.println("executer: " + new String(msg.getBody()) + " rollback");
			return LocalTransactionState.ROLLBACK_MESSAGE;
		}
		System.out.println("executer: " + new String(msg.getBody()) + " commit");
		return LocalTransactionState.COMMIT_MESSAGE;
	}

	/**
	 * MQ check back local transaction states.
	 * @param msg messages
	 * @return Transaction state
	 */
	@Override
	public LocalTransactionState checkLocalTransaction(MessageExt msg) {
		System.out.println("check: " + new String(msg.getBody()));
		return LocalTransactionState.COMMIT_MESSAGE;
	}

}
