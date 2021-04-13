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

package com.alibaba.cloud.stream.binder.rocketmq.consuming;

import java.util.List;
import java.util.Objects;

import com.alibaba.cloud.stream.binder.rocketmq.RocketMQBinderUtils;
import com.alibaba.cloud.stream.binder.rocketmq.RocketMQMessageChannelBinder;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQHeaderMapper;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.apache.rocketmq.spring.support.RocketMQListenerContainer;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static com.alibaba.cloud.stream.binder.rocketmq.RocketMQBinderConstants.ROCKETMQ_RECONSUME_TIMES;

/**
 * A class that Listen on rocketmq message.
 * <p>
 * this class will delegate {@link RocketMQListener} to handle message
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @author <a href="mailto:jiashuai.xie01@gmail.com">Xiejiashuai</a>
 * @see RocketMQListener
 */
public class RocketMQListenerBindingContainer
		implements InitializingBean, RocketMQListenerContainer, SmartLifecycle {

	private final static Logger log = LoggerFactory
			.getLogger(RocketMQListenerBindingContainer.class);

	private long suspendCurrentQueueTimeMillis = 1000;

	/**
	 * Message consume retry strategy<br>
	 * -1,no retry,put into DLQ directly<br>
	 * 0,broker control retry frequency<br>
	 * >0,client control retry frequency.
	 */
	private int delayLevelWhenNextConsume = 0;

	private List<String> nameServer;

	private String consumerGroup;

	private String topic;

	private int consumeThreadMax = 64;

	private String charset = "UTF-8";

	private RocketMQListener rocketMQListener;

	private RocketMQHeaderMapper headerMapper;

	private DefaultMQPushConsumer consumer;

	private boolean running;

	private final ExtendedConsumerProperties<RocketMQConsumerProperties> rocketMQConsumerProperties;

	private final RocketMQMessageChannelBinder rocketMQMessageChannelBinder;

	private final RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties;

	// The following properties came from RocketMQConsumerProperties.
	private ConsumeMode consumeMode;

	private SelectorType selectorType;

	private String selectorExpression;

	private MessageModel messageModel;

	public RocketMQListenerBindingContainer(
			ExtendedConsumerProperties<RocketMQConsumerProperties> rocketMQConsumerProperties,
			RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties,
			RocketMQMessageChannelBinder rocketMQMessageChannelBinder) {
		this.rocketMQConsumerProperties = rocketMQConsumerProperties;
		this.rocketBinderConfigurationProperties = rocketBinderConfigurationProperties;
		this.rocketMQMessageChannelBinder = rocketMQMessageChannelBinder;
		this.consumeMode = rocketMQConsumerProperties.getExtension().getOrderly()
				? ConsumeMode.ORDERLY : ConsumeMode.CONCURRENTLY;
		if (StringUtils.isEmpty(rocketMQConsumerProperties.getExtension().getSql())) {
			this.selectorType = SelectorType.TAG;
			this.selectorExpression = rocketMQConsumerProperties.getExtension().getTags();
		}
		else {
			this.selectorType = SelectorType.SQL92;
			this.selectorExpression = rocketMQConsumerProperties.getExtension().getSql();
		}
		this.messageModel = rocketMQConsumerProperties.getExtension().getBroadcasting()
				? MessageModel.BROADCASTING : MessageModel.CLUSTERING;
	}

	@Override
	public void setupMessageListener(RocketMQListener<?> rocketMQListener) {
		this.rocketMQListener = rocketMQListener;
	}

	@Override
	public void destroy() throws Exception {
		this.setRunning(false);
		if (Objects.nonNull(consumer)) {
			consumer.shutdown();
		}
		log.info("container destroyed, {}", this.toString());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initRocketMQPushConsumer();
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	@Override
	public void start() {
		if (this.isRunning()) {
			throw new IllegalStateException(
					"container already running. " + this.toString());
		}

		try {
			consumer.start();
		}
		catch (MQClientException e) {
			throw new IllegalStateException("Failed to start RocketMQ push consumer", e);
		}
		this.setRunning(true);

		log.info("running container: {}", this.toString());
	}

	@Override
	public void stop() {
		if (this.isRunning()) {
			if (Objects.nonNull(consumer)) {
				consumer.shutdown();
			}
			setRunning(false);
		}
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	private void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}

	private void initRocketMQPushConsumer() throws MQClientException {
		Assert.notNull(rocketMQListener, "Property 'rocketMQListener' is required");
		Assert.notNull(consumerGroup, "Property 'consumerGroup' is required");
		Assert.notNull(nameServer, "Property 'nameServer' is required");
		Assert.notNull(topic, "Property 'topic' is required");

		String ak = rocketBinderConfigurationProperties.getAccessKey();
		String sk = rocketBinderConfigurationProperties.getSecretKey();
		if (!StringUtils.isEmpty(ak) && !StringUtils.isEmpty(sk)) {
			RPCHook rpcHook = new AclClientRPCHook(new SessionCredentials(ak, sk));
			consumer = new DefaultMQPushConsumer(consumerGroup, rpcHook,
					new AllocateMessageQueueAveragely(),
					rocketBinderConfigurationProperties.isEnableMsgTrace(),
					rocketBinderConfigurationProperties.getCustomizedTraceTopic());
			consumer.setInstanceName(RocketMQUtil.getInstanceName(rpcHook,
					topic + "|" + UtilAll.getPid()));
			consumer.setVipChannelEnabled(false);
		}
		else {
			consumer = new DefaultMQPushConsumer(consumerGroup,
					rocketBinderConfigurationProperties.isEnableMsgTrace(),
					rocketBinderConfigurationProperties.getCustomizedTraceTopic());
		}

		consumer.setNamesrvAddr(RocketMQBinderUtils.getNameServerStr(nameServer));
		consumer.setConsumeThreadMax(rocketMQConsumerProperties.getConcurrency());
		consumer.setConsumeThreadMin(rocketMQConsumerProperties.getConcurrency());

		if (!StringUtils.isEmpty(rocketBinderConfigurationProperties.getAccessChannel())) {
			consumer.setAccessChannel(AccessChannel.valueOf(rocketBinderConfigurationProperties.getAccessChannel()));
		}

		switch (messageModel) {
		case BROADCASTING:
			consumer.setMessageModel(
					org.apache.rocketmq.common.protocol.heartbeat.MessageModel.BROADCASTING);
			break;
		case CLUSTERING:
			consumer.setMessageModel(
					org.apache.rocketmq.common.protocol.heartbeat.MessageModel.CLUSTERING);
			break;
		default:
			throw new IllegalArgumentException("Property 'messageModel' was wrong.");
		}

		switch (selectorType) {
		case TAG:
			consumer.subscribe(topic, selectorExpression);
			break;
		case SQL92:
			consumer.subscribe(topic, MessageSelector.bySql(selectorExpression));
			break;
		default:
			throw new IllegalArgumentException("Property 'selectorType' was wrong.");
		}

		switch (consumeMode) {
		case ORDERLY:
			consumer.setMessageListener(new DefaultMessageListenerOrderly());
			break;
		case CONCURRENTLY:
			consumer.setMessageListener(new DefaultMessageListenerConcurrently());
			break;
		default:
			throw new IllegalArgumentException("Property 'consumeMode' was wrong.");
		}

		if (rocketMQListener instanceof RocketMQPushConsumerLifecycleListener) {
			((RocketMQPushConsumerLifecycleListener) rocketMQListener)
					.prepareStart(consumer);
		}

	}

	@Override
	public String toString() {
		return "RocketMQListenerBindingContainer{" + "consumerGroup='" + consumerGroup
				+ '\'' + ", nameServer='" + nameServer + '\'' + ", topic='" + topic + '\''
				+ ", consumeMode=" + consumeMode + ", selectorType=" + selectorType
				+ ", selectorExpression='" + selectorExpression + '\'' + ", messageModel="
				+ messageModel + '}';
	}

	public long getSuspendCurrentQueueTimeMillis() {
		return suspendCurrentQueueTimeMillis;
	}

	public void setSuspendCurrentQueueTimeMillis(long suspendCurrentQueueTimeMillis) {
		this.suspendCurrentQueueTimeMillis = suspendCurrentQueueTimeMillis;
	}

	public int getDelayLevelWhenNextConsume() {
		return delayLevelWhenNextConsume;
	}

	public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
		this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
	}

	public List<String> getNameServer() {
		return nameServer;
	}

	public void setNameServer(List<String> nameServer) {
		this.nameServer = nameServer;
	}

	public String getConsumerGroup() {
		return consumerGroup;
	}

	public void setConsumerGroup(String consumerGroup) {
		this.consumerGroup = consumerGroup;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getConsumeThreadMax() {
		return consumeThreadMax;
	}

	public void setConsumeThreadMax(int consumeThreadMax) {
		this.consumeThreadMax = consumeThreadMax;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public RocketMQListener getRocketMQListener() {
		return rocketMQListener;
	}

	public void setRocketMQListener(RocketMQListener rocketMQListener) {
		this.rocketMQListener = rocketMQListener;
	}

	public DefaultMQPushConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(DefaultMQPushConsumer consumer) {
		this.consumer = consumer;
	}

	public ExtendedConsumerProperties<RocketMQConsumerProperties> getRocketMQConsumerProperties() {
		return rocketMQConsumerProperties;
	}

	public ConsumeMode getConsumeMode() {
		return consumeMode;
	}

	public SelectorType getSelectorType() {
		return selectorType;
	}

	public String getSelectorExpression() {
		return selectorExpression;
	}

	public MessageModel getMessageModel() {
		return messageModel;
	}

	public RocketMQHeaderMapper getHeaderMapper() {
		return headerMapper;
	}

	public void setHeaderMapper(RocketMQHeaderMapper headerMapper) {
		this.headerMapper = headerMapper;
	}

	/**
	 * Convert rocketmq {@link MessageExt} to Spring {@link Message}.
	 * @param messageExt the rocketmq message
	 * @return the converted Spring {@link Message}
	 */
	@SuppressWarnings("unchecked")
	private Message convertToSpringMessage(MessageExt messageExt) {

		// add reconsume-times header to messageExt
		int reconsumeTimes = messageExt.getReconsumeTimes();
		messageExt.putUserProperty(ROCKETMQ_RECONSUME_TIMES,
				String.valueOf(reconsumeTimes));
		Message message = RocketMQUtil.convertToSpringMessage(messageExt);
		return MessageBuilder.fromMessage(message)
				.copyHeaders(headerMapper.toHeaders(messageExt.getProperties())).build();
	}

	public class DefaultMessageListenerConcurrently
			implements MessageListenerConcurrently {

		@SuppressWarnings({ "unchecked", "Duplicates" })
		@Override
		public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
				ConsumeConcurrentlyContext context) {
			for (MessageExt messageExt : msgs) {
				log.debug("received msg: {}", messageExt);
				try {
					long now = System.currentTimeMillis();
					rocketMQListener.onMessage(convertToSpringMessage(messageExt));
					long costTime = System.currentTimeMillis() - now;
					log.debug("consume {} message key:[{}] cost: {} ms",
							messageExt.getMsgId(), messageExt.getKeys(), costTime);
				}
				catch (Exception e) {
					log.warn("consume message failed. messageExt:{}", messageExt, e);
					context.setDelayLevelWhenNextConsume(delayLevelWhenNextConsume);
					return ConsumeConcurrentlyStatus.RECONSUME_LATER;
				}
			}

			return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
		}

	}

	public class DefaultMessageListenerOrderly implements MessageListenerOrderly {

		@SuppressWarnings({ "unchecked", "Duplicates" })
		@Override
		public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs,
				ConsumeOrderlyContext context) {
			for (MessageExt messageExt : msgs) {
				log.debug("received msg: {}", messageExt);
				try {
					long now = System.currentTimeMillis();
					rocketMQListener.onMessage(convertToSpringMessage(messageExt));
					long costTime = System.currentTimeMillis() - now;
					log.info("consume {} message key:[{}] cost: {} ms",
							messageExt.getMsgId(), messageExt.getKeys(), costTime);
				}
				catch (Exception e) {
					log.warn("consume message failed. messageExt:{}", messageExt, e);
					context.setSuspendCurrentQueueTimeMillis(
							suspendCurrentQueueTimeMillis);
					return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
				}
			}

			return ConsumeOrderlyStatus.SUCCESS;
		}

	}

}
