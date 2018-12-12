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

package org.springframework.cloud.stream.binder.rocketmq.properties;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.LocalTransactionExecuter;
import org.apache.rocketmq.client.producer.TransactionCheckListener;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQProducerProperties {

	private Boolean enabled = true;

	/**
	 * Maximum allowed message size in bytes {@link DefaultMQProducer#maxMessageSize}
	 */
	private Integer maxMessageSize = 0;

	private Boolean transactional = false;

	/**
	 * full class name of {@link LocalTransactionExecuter}
	 */
	private String executer;

	/**
	 * full class name of {@link TransactionCheckListener}
	 */
	private String transactionCheckListener;

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Integer getMaxMessageSize() {
		return maxMessageSize;
	}

	public void setMaxMessageSize(Integer maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

	public Boolean getTransactional() {
		return transactional;
	}

	public void setTransactional(Boolean transactional) {
		this.transactional = transactional;
	}

	public String getExecuter() {
		return executer;
	}

	public void setExecuter(String executer) {
		this.executer = executer;
	}

	public String getTransactionCheckListener() {
		return transactionCheckListener;
	}

	public void setTransactionCheckListener(String transactionCheckListener) {
		this.transactionCheckListener = transactionCheckListener;
	}
}
