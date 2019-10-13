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

package com.alibaba.cloud.stream.binder.rocketmq.properties;

import java.util.Set;

import com.alibaba.cloud.stream.binder.rocketmq.support.JacksonRocketMQHeaderMapper;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQConsumerProperties {

	/**
	 * using '||' to split tag {@link MQPushConsumer#subscribe(String, String)}.
	 */
	private String tags;

	/**
	 * {@link MQPushConsumer#subscribe(String, MessageSelector)}
	 * {@link MessageSelector#bySql(String)}.
	 */
	private String sql;

	/**
	 * {@link MessageModel#BROADCASTING}.
	 */
	private Boolean broadcasting = false;

	/**
	 * if orderly is true, using {@link MessageListenerOrderly} else if orderly if false,
	 * using {@link MessageListenerConcurrently}.
	 */
	private Boolean orderly = false;

	/**
	 * for concurrently listener. message consume retry strategy. see
	 * {@link ConsumeConcurrentlyContext#delayLevelWhenNextConsume}. -1 means dlq(or
	 * discard, see {@link this#shouldRequeue}), others means requeue.
	 */
	private int delayLevelWhenNextConsume = 0;

	/**
	 * for orderly listener. next retry delay time.
	 */
	private long suspendCurrentQueueTimeMillis = 1000;

	private Boolean enabled = true;

	/**
	 * {@link JacksonRocketMQHeaderMapper#addTrustedPackages(String...)}.
	 */
	private Set<String> trustedPackages;

	// ------------ For Pull Consumer ------------

	private long pullTimeout = 10 * 1000;

	private boolean fromStore;

	// ------------ For Pull Consumer ------------

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Boolean getOrderly() {
		return orderly;
	}

	public void setOrderly(Boolean orderly) {
		this.orderly = orderly;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getBroadcasting() {
		return broadcasting;
	}

	public void setBroadcasting(Boolean broadcasting) {
		this.broadcasting = broadcasting;
	}

	public int getDelayLevelWhenNextConsume() {
		return delayLevelWhenNextConsume;
	}

	public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
		this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
	}

	public long getSuspendCurrentQueueTimeMillis() {
		return suspendCurrentQueueTimeMillis;
	}

	public void setSuspendCurrentQueueTimeMillis(long suspendCurrentQueueTimeMillis) {
		this.suspendCurrentQueueTimeMillis = suspendCurrentQueueTimeMillis;
	}

	public long getPullTimeout() {
		return pullTimeout;
	}

	public void setPullTimeout(long pullTimeout) {
		this.pullTimeout = pullTimeout;
	}

	public boolean isFromStore() {
		return fromStore;
	}

	public void setFromStore(boolean fromStore) {
		this.fromStore = fromStore;
	}

	public boolean shouldRequeue() {
		return delayLevelWhenNextConsume != -1;
	}

	public Set<String> getTrustedPackages() {
		return trustedPackages;
	}

	public void setTrustedPackages(Set<String> trustedPackages) {
		this.trustedPackages = trustedPackages;
	}

}
