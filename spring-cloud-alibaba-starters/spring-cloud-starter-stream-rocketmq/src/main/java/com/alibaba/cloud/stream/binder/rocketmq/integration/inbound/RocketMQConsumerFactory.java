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

package com.alibaba.cloud.stream.binder.rocketmq.integration.inbound;

import com.alibaba.cloud.stream.binder.rocketmq.custom.RocketMQBeanContainerCache;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.utils.RocketMQUtils;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.RPCHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Extended function related to producer . eg:initial
 *
 * @author zkzlx
 */
public final class RocketMQConsumerFactory {

	private RocketMQConsumerFactory() {
	}

	private final static Logger log = LoggerFactory
			.getLogger(RocketMQConsumerFactory.class);

	public static DefaultMQPushConsumer initPushConsumer(
			ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties) {
		RocketMQConsumerProperties consumerProperties = extendedConsumerProperties
				.getExtension();
		Assert.notNull(consumerProperties.getGroup(),
				"Property 'group' is required - consumerGroup");
		Assert.notNull(consumerProperties.getNameServer(),
				"Property 'nameServer' is required");
		AllocateMessageQueueStrategy allocateMessageQueueStrategy = RocketMQBeanContainerCache
				.getBean(consumerProperties.getAllocateMessageQueueStrategy(),
						AllocateMessageQueueStrategy.class,
						new AllocateMessageQueueAveragely());
		RPCHook rpcHook = null;
		if (!StringUtils.isEmpty(consumerProperties.getAccessKey())
				&& !StringUtils.isEmpty(consumerProperties.getSecretKey())) {
			rpcHook = new AclClientRPCHook(
					new SessionCredentials(consumerProperties.getAccessKey(),
							consumerProperties.getSecretKey()));
		}
		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(
				consumerProperties.getGroup(), rpcHook, allocateMessageQueueStrategy,
				consumerProperties.getEnableMsgTrace(),
				consumerProperties.getCustomizedTraceTopic());
		consumer.setVipChannelEnabled(
				null == rpcHook && consumerProperties.getVipChannelEnabled());
		consumer.setInstanceName(
				RocketMQUtils.getInstanceName(rpcHook, consumerProperties.getGroup()));
		consumer.setNamespace(consumerProperties.getNamespace());
		consumer.setNamesrvAddr(consumerProperties.getNameServer());
		consumer.setMessageModel(getMessageModel(consumerProperties.getMessageModel()));
		consumer.setUseTLS(consumerProperties.getUseTLS());
		consumer.setPullTimeDelayMillsWhenException(
				consumerProperties.getPullTimeDelayMillsWhenException());
		consumer.setPullBatchSize(consumerProperties.getPullBatchSize());
		consumer.setConsumeFromWhere(consumerProperties.getConsumeFromWhere());
		consumer.setHeartbeatBrokerInterval(
				consumerProperties.getHeartbeatBrokerInterval());
		consumer.setPersistConsumerOffsetInterval(
				consumerProperties.getPersistConsumerOffsetInterval());
		consumer.setPullInterval(consumerProperties.getPush().getPullInterval());
		consumer.setConsumeThreadMin(extendedConsumerProperties.getConcurrency());
		consumer.setConsumeThreadMax(extendedConsumerProperties.getConcurrency());
		consumer.setUnitName(consumerProperties.getUnitName());
		return consumer;
	}

	/**
	 * todo Compatible with versions less than 4.6 ?
	 * @param extendedConsumerProperties extendedConsumerProperties
	 * @return DefaultLitePullConsumer
	 */
	public static DefaultLitePullConsumer initPullConsumer(
			ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties) {
		RocketMQConsumerProperties consumerProperties = extendedConsumerProperties
				.getExtension();
		Assert.notNull(consumerProperties.getGroup(),
				"Property 'group' is required - consumerGroup");
		Assert.notNull(consumerProperties.getNameServer(),
				"Property 'nameServer' is required");
		AllocateMessageQueueStrategy allocateMessageQueueStrategy = RocketMQBeanContainerCache
				.getBean(consumerProperties.getAllocateMessageQueueStrategy(),
						AllocateMessageQueueStrategy.class,
						new AllocateMessageQueueAveragely());

		RPCHook rpcHook = null;
		if (!StringUtils.isEmpty(consumerProperties.getAccessKey())
				&& !StringUtils.isEmpty(consumerProperties.getSecretKey())) {
			rpcHook = new AclClientRPCHook(
					new SessionCredentials(consumerProperties.getAccessKey(),
							consumerProperties.getSecretKey()));
		}

		DefaultLitePullConsumer consumer = new DefaultLitePullConsumer(
				consumerProperties.getNamespace(), consumerProperties.getGroup(),
				rpcHook);
		consumer.setVipChannelEnabled(
				null == rpcHook && consumerProperties.getVipChannelEnabled());
		consumer.setInstanceName(
				RocketMQUtils.getInstanceName(rpcHook, consumerProperties.getGroup()));
		consumer.setAllocateMessageQueueStrategy(allocateMessageQueueStrategy);
		consumer.setNamesrvAddr(consumerProperties.getNameServer());
		consumer.setMessageModel(getMessageModel(consumerProperties.getMessageModel()));
		consumer.setUseTLS(consumerProperties.getUseTLS());
		consumer.setPullTimeDelayMillsWhenException(
				consumerProperties.getPullTimeDelayMillsWhenException());
		consumer.setConsumerTimeoutMillisWhenSuspend(
				consumerProperties.getPull().getConsumerTimeoutMillisWhenSuspend());
		consumer.setPullBatchSize(consumerProperties.getPullBatchSize());
		consumer.setConsumeFromWhere(consumerProperties.getConsumeFromWhere());
		consumer.setHeartbeatBrokerInterval(
				consumerProperties.getHeartbeatBrokerInterval());
		consumer.setPersistConsumerOffsetInterval(
				consumerProperties.getPersistConsumerOffsetInterval());
		consumer.setPollTimeoutMillis(
				consumerProperties.getPull().getPollTimeoutMillis());
		consumer.setPullThreadNums(extendedConsumerProperties.getConcurrency());
		// The internal queues are cached by a maximum of 1000
		consumer.setPullThresholdForAll(extendedConsumerProperties.getExtension()
				.getPull().getPullThresholdForAll());
		consumer.setUnitName(consumerProperties.getUnitName());
		return consumer;
	}

	private static MessageModel getMessageModel(String messageModel) {
		for (MessageModel model : MessageModel.values()) {
			if (model.getModeCN().equalsIgnoreCase(messageModel)) {
				return model;
			}
		}
		return MessageModel.CLUSTERING;
	}

}
