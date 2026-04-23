/*
 * Copyright 2013-present the original author or authors.
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
import org.jspecify.annotations.Nullable;

/**
 * @author zkzlx
 */
public class RocketMQCommonProperties implements Serializable {

	private static final long serialVersionUID = -6724870154343284715L;

	private boolean enabled = true;

	private @Nullable String nameServer;

	/**
	 * The property of "access-key".
	 */
	private @Nullable String accessKey;

	/**
	 * The property of "secret-key".
	 */
	private @Nullable String secretKey;

	/**
	 * Consumers of the same role is required to have exactly same subscriptions and
	 * consumerGroup to correctly achieve load balance. It's required and needs to be
	 * globally unique. Producer group conceptually aggregates all producer instances of
	 * exactly same role, which is particularly important when transactional messages are
	 * involved. For non-transactional messages, it does not matter as long as it's unique
	 * per process. See <a href="http://rocketmq.apache.org/docs/core-concept/">here</a>
	 * for further discussion.
	 */
	private @Nullable String group;

	private @Nullable String namespace;

	private @Nullable String namespaceV2;

	/**
	 * The property of "unitName".
	 */
	private @Nullable String unitName;

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

	private @Nullable String customizedTraceTopic;

	/**
	 * Whether to share a single {@link MQClientInstance} across producers and
	 * consumers that connect to the same name server. When {@code true}, the
	 * per-client {@code nanoTime} suffix is omitted from the generated
	 * instance name so RocketMQ can reuse one {@link MQClientInstance} and
	 * its worker threads instead of spawning ~20 threads per binding.
	 * <p>{@code null} (the default) preserves the historical per-client
	 * isolation on the extension side while still allowing binder-level
	 * configuration to apply; an explicit {@code false} on the extension
	 * takes precedence over a binder-level {@code true}, giving bindings
	 * that need isolated instances (e.g. ordered/transactional producers)
	 * a way to opt out.
	 */
	private @Nullable Boolean shareClientInstance;

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public @Nullable String getNameServer() {
		return nameServer;
	}

	public void setNameServer(@Nullable String nameServer) {
		this.nameServer = nameServer;
	}

	public @Nullable String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(@Nullable String accessKey) {
		this.accessKey = accessKey;
	}

	public @Nullable String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(@Nullable String secretKey) {
		this.secretKey = secretKey;
	}

	public @Nullable String getGroup() {
		return group;
	}

	public void setGroup(@Nullable String group) {
		this.group = group;
	}

	public @Nullable String getNamespace() {
		return namespace;
	}

	public void setNamespace(@Nullable String namespace) {
		this.namespace = namespace;
	}

	public @Nullable String getNamespaceV2() {
		return namespaceV2;
	}

	public void setNamespaceV2(@Nullable String namespaceV2) {
		this.namespaceV2 = namespaceV2;
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

	public @Nullable String getCustomizedTraceTopic() {
		return customizedTraceTopic;
	}

	public void setCustomizedTraceTopic(@Nullable String customizedTraceTopic) {
		this.customizedTraceTopic = customizedTraceTopic;
	}

	public @Nullable String getUnitName() {
		return unitName;
	}

	public void setUnitName(@Nullable String unitName) {
		this.unitName = unitName;
	}

	public @Nullable Boolean getShareClientInstance() {
		return shareClientInstance;
	}

	public void setShareClientInstance(@Nullable Boolean shareClientInstance) {
		this.shareClientInstance = shareClientInstance;
	}
}
