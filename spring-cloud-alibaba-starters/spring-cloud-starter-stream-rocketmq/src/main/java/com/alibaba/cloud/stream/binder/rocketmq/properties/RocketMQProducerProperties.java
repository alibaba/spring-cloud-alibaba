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

import org.apache.rocketmq.client.producer.DefaultMQProducer;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQProducerProperties {

	private Boolean enabled = true;

	/**
	 * Name of producer.
	 */
	private String group;

	/**
	 * Maximum allowed message size in bytes {@link DefaultMQProducer#maxMessageSize}.
	 */
	private Integer maxMessageSize = 1024 * 1024 * 4;

	private Boolean transactional = false;

	private Boolean sync = false;

	private Boolean vipChannelEnabled = true;

	/**
	 * Millis of send message timeout.
	 */
	private int sendMessageTimeout = 3000;

	/**
	 * Compress message body threshold, namely, message body larger than 4k will be
	 * compressed on default.
	 */
	private int compressMessageBodyThreshold = 1024 * 4;

	/**
	 * Maximum number of retry to perform internally before claiming sending failure in
	 * synchronous mode. This may potentially cause message duplication which is up to
	 * application developers to resolve.
	 */
	private int retryTimesWhenSendFailed = 2;

	/**
	 * <p>
	 * Maximum number of retry to perform internally before claiming sending failure in
	 * asynchronous mode.
	 * </p>
	 * This may potentially cause message duplication which is up to application
	 * developers to resolve.
	 */
	private int retryTimesWhenSendAsyncFailed = 2;

	/**
	 * Indicate whether to retry another broker on sending failure internally.
	 */
	private boolean retryNextServer = false;

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

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

	public Boolean getSync() {
		return sync;
	}

	public void setSync(Boolean sync) {
		this.sync = sync;
	}

	public Boolean getVipChannelEnabled() {
		return vipChannelEnabled;
	}

	public void setVipChannelEnabled(Boolean vipChannelEnabled) {
		this.vipChannelEnabled = vipChannelEnabled;
	}

	public int getSendMessageTimeout() {
		return sendMessageTimeout;
	}

	public void setSendMessageTimeout(int sendMessageTimeout) {
		this.sendMessageTimeout = sendMessageTimeout;
	}

	public int getCompressMessageBodyThreshold() {
		return compressMessageBodyThreshold;
	}

	public void setCompressMessageBodyThreshold(int compressMessageBodyThreshold) {
		this.compressMessageBodyThreshold = compressMessageBodyThreshold;
	}

	public int getRetryTimesWhenSendFailed() {
		return retryTimesWhenSendFailed;
	}

	public void setRetryTimesWhenSendFailed(int retryTimesWhenSendFailed) {
		this.retryTimesWhenSendFailed = retryTimesWhenSendFailed;
	}

	public int getRetryTimesWhenSendAsyncFailed() {
		return retryTimesWhenSendAsyncFailed;
	}

	public void setRetryTimesWhenSendAsyncFailed(int retryTimesWhenSendAsyncFailed) {
		this.retryTimesWhenSendAsyncFailed = retryTimesWhenSendAsyncFailed;
	}

	public boolean isRetryNextServer() {
		return retryNextServer;
	}

	public void setRetryNextServer(boolean retryNextServer) {
		this.retryNextServer = retryNextServer;
	}

}
