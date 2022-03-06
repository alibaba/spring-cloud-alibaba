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

import java.io.Serializable;

import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.remoting.netty.TlsSystemConfig;

/**
 * @author zkzlx
 */
public class RocketMQCommonProperties implements Serializable {

	private static final long serialVersionUID = -6724870154343284715L;

	private boolean enabled = true;

	private String nameServer;

	/**
	 * The property of "access-key".
	 */
	private String accessKey;

	/**
	 * The property of "secret-key".
	 */
	private String secretKey;

	/**
	 * Consumers of the same role is required to have exactly same subscriptions and
	 * consumerGroup to correctly achieve load balance. It's required and needs to be
	 * globally unique. Producer group conceptually aggregates all producer instances of
	 * exactly same role, which is particularly important when transactional messages are
	 * involved. For non-transactional messages, it does not matter as long as it's unique
	 * per process. See <a href="http://rocketmq.apache.org/docs/core-concept/">here</a>
	 * for further discussion.
	 */
	private String group;

	private String namespace;

	/**
	 * The property of "unitName".
	 */
	private String unitName;

	private String accessChannel = AccessChannel.LOCAL.name();

	/**
	 * Pulling topic information interval from the named server.
	 * see{@link MQClientInstance#startScheduledTask()},eg:ScheduledTask
	 * updateTopicRouteInfoFromNameServer.
	 */
	private int pollNameServerInterval = 1000 * 30;

	/**
	 * Heartbeat interval in microseconds with message broker.
	 * see{@link MQClientInstance#startScheduledTask()},eg:ScheduledTask
	 * sendHeartbeatToAllBroker .
	 */
	private int heartbeatBrokerInterval = 1000 * 30;

	/**
	 * Offset persistent interval for consumer.
	 * see{@link MQClientInstance#startScheduledTask()},eg:ScheduledTask
	 * sendHeartbeatToAllBroker .
	 */
	private int persistConsumerOffsetInterval = 1000 * 5;

	private boolean vipChannelEnabled = false;

	private boolean useTLS = TlsSystemConfig.tlsEnable;

	private boolean enableMsgTrace = true;

	private String customizedTraceTopic;

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getNameServer() {
		return nameServer;
	}

	public void setNameServer(String nameServer) {
		this.nameServer = nameServer;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getAccessChannel() {
		return accessChannel;
	}

	public void setAccessChannel(String accessChannel) {
		this.accessChannel = accessChannel;
	}

	public int getPollNameServerInterval() {
		return pollNameServerInterval;
	}

	public void setPollNameServerInterval(int pollNameServerInterval) {
		this.pollNameServerInterval = pollNameServerInterval;
	}

	public int getHeartbeatBrokerInterval() {
		return heartbeatBrokerInterval;
	}

	public void setHeartbeatBrokerInterval(int heartbeatBrokerInterval) {
		this.heartbeatBrokerInterval = heartbeatBrokerInterval;
	}

	public int getPersistConsumerOffsetInterval() {
		return persistConsumerOffsetInterval;
	}

	public void setPersistConsumerOffsetInterval(int persistConsumerOffsetInterval) {
		this.persistConsumerOffsetInterval = persistConsumerOffsetInterval;
	}

	public boolean getVipChannelEnabled() {
		return vipChannelEnabled;
	}

	public void setVipChannelEnabled(boolean vipChannelEnabled) {
		this.vipChannelEnabled = vipChannelEnabled;
	}

	public boolean getUseTLS() {
		return useTLS;
	}

	public void setUseTLS(boolean useTLS) {
		this.useTLS = useTLS;
	}

	public boolean getEnableMsgTrace() {
		return enableMsgTrace;
	}

	public void setEnableMsgTrace(boolean enableMsgTrace) {
		this.enableMsgTrace = enableMsgTrace;
	}

	public String getCustomizedTraceTopic() {
		return customizedTraceTopic;
	}

	public void setCustomizedTraceTopic(String customizedTraceTopic) {
		this.customizedTraceTopic = customizedTraceTopic;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
}
