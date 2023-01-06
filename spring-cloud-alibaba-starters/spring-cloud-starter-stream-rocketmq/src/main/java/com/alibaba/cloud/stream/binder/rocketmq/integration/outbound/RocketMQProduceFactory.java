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

package com.alibaba.cloud.stream.binder.rocketmq.integration.outbound;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.cloud.stream.binder.rocketmq.constant.RocketMQConst;
import com.alibaba.cloud.stream.binder.rocketmq.custom.RocketMQBeanContainerCache;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.utils.RocketMQUtils;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.hook.CheckForbiddenHook;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.client.trace.TraceDispatcher;
import org.apache.rocketmq.client.trace.hook.SendMessageTraceHookImpl;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Extended function related to producer . eg:initial .
 *
 * @author zkzlx
 */
public final class RocketMQProduceFactory {

	private final static Logger log = LoggerFactory
			.getLogger(RocketMQProduceFactory.class);

	private static final Map<String, DefaultMQProducer> PRODUCER_REUSABLE_MAP = new ConcurrentHashMap<>();

	private RocketMQProduceFactory() {
	}

	/**
	 * init for the producer,including convert producer params.
	 * @param topic topic
	 * @param producerProperties producerProperties
	 * @return DefaultMQProducer
	 */
	public static DefaultMQProducer initRocketMQProducer(String topic,
			RocketMQProducerProperties producerProperties) {
		if (StringUtils.isEmpty(producerProperties.getGroup())) {
			producerProperties.setGroup(RocketMQConst.DEFAULT_GROUP);
		}
		Assert.notNull(producerProperties.getGroup(),
				"Property 'group' is required - producerGroup");
		Assert.notNull(producerProperties.getNameServer(),
				"Property 'nameServer' is required");
		RPCHook rpcHook = null;
		if (!StringUtils.isEmpty(producerProperties.getAccessKey())
				&& !StringUtils.isEmpty(producerProperties.getSecretKey())) {
			rpcHook = new AclClientRPCHook(
					new SessionCredentials(producerProperties.getAccessKey(),
							producerProperties.getSecretKey()));
		}
		DefaultMQProducer producer;
		if (RocketMQProducerProperties.ProducerType.Trans
				.equalsName(producerProperties.getProducerType())) {
			producer = new TransactionMQProducer(producerProperties.getNamespace(),
					producerProperties.getGroup(), rpcHook);
			if (producerProperties.getEnableMsgTrace()) {
				try {
					AsyncTraceDispatcher dispatcher = new AsyncTraceDispatcher(
							producerProperties.getGroup(), TraceDispatcher.Type.PRODUCE,
							producerProperties.getCustomizedTraceTopic(), rpcHook);
					dispatcher.setHostProducer(producer.getDefaultMQProducerImpl());
					Field field = DefaultMQProducer.class
							.getDeclaredField("traceDispatcher");
					field.setAccessible(true);
					field.set(producer, dispatcher);
					producer.getDefaultMQProducerImpl().registerSendMessageHook(
							new SendMessageTraceHookImpl(dispatcher));
				}
				catch (Throwable e) {
					log.error(
							"system mq-trace hook init failed ,maybe can't send msg trace data");
				}
			}
		}
		else {
			String key = getKey(producerProperties);
			if (PRODUCER_REUSABLE_MAP.containsKey(key)) {
				return PRODUCER_REUSABLE_MAP.get(key);
			}
			producer = new ReusableMQProducer(producerProperties.getNamespace(),
					producerProperties.getGroup(), rpcHook,
					producerProperties.getEnableMsgTrace(),
					producerProperties.getCustomizedTraceTopic(), key);
			PRODUCER_REUSABLE_MAP.put(key, producer);
		}

		producer.setVipChannelEnabled(
				null == rpcHook && producerProperties.getVipChannelEnabled());
		producer.setInstanceName(
				RocketMQUtils.getInstanceName(rpcHook, topic + "|" + UtilAll.getPid()));
		producer.setNamesrvAddr(producerProperties.getNameServer());
		producer.setSendMsgTimeout(producerProperties.getSendMsgTimeout());
		producer.setRetryTimesWhenSendFailed(
				producerProperties.getRetryTimesWhenSendFailed());
		producer.setRetryTimesWhenSendAsyncFailed(
				producerProperties.getRetryTimesWhenSendAsyncFailed());
		producer.setCompressMsgBodyOverHowmuch(
				producerProperties.getCompressMsgBodyThreshold());
		producer.setRetryAnotherBrokerWhenNotStoreOK(
				producerProperties.getRetryAnotherBroker());
		producer.setMaxMessageSize(producerProperties.getMaxMessageSize());
		producer.setUseTLS(producerProperties.getUseTLS());
		producer.setUnitName(producerProperties.getUnitName());
		CheckForbiddenHook checkForbiddenHook = RocketMQBeanContainerCache.getBean(
				producerProperties.getCheckForbiddenHook(), CheckForbiddenHook.class);
		if (null != checkForbiddenHook) {
			producer.getDefaultMQProducerImpl()
					.registerCheckForbiddenHook(checkForbiddenHook);
		}
		SendMessageHook sendMessageHook = RocketMQBeanContainerCache
				.getBean(producerProperties.getSendMessageHook(), SendMessageHook.class);
		if (null != sendMessageHook) {
			producer.getDefaultMQProducerImpl().registerSendMessageHook(sendMessageHook);
		}

		return producer;
	}

	/**
	 * get the key from producerProperties.
	 * @param producerProperties producer properties
	 * @return key
	 */
	private static String getKey(RocketMQProducerProperties producerProperties) {
		return producerProperties.getNameServer() + "," + producerProperties.getGroup()
				+ producerProperties.getSendCallBack();
	}

	/**
	 * This is a special kind of MQProducer that can be reused among different threads.
	 * The start and shutdown method can be invoked multiple times, but the real start and
	 * shutdown logics will only be executed once.
	 */
	protected static class ReusableMQProducer extends DefaultMQProducer {

		private final AtomicInteger atomicInteger = new AtomicInteger();

		private final String key;

		public ReusableMQProducer(String namespace, String group, RPCHook rpcHook,
				boolean enableMsgTrace, String customizedTraceTopic, String key) {
			super(namespace, group, rpcHook, enableMsgTrace, customizedTraceTopic);
			this.key = key;
		}

		@Override
		public void start() throws MQClientException {
			if (atomicInteger.getAndIncrement() == 0) {
				super.start();
			}
		}

		@Override
		public void shutdown() {
			if (atomicInteger.decrementAndGet() == 0) {
				PRODUCER_REUSABLE_MAP.remove(key);
				super.shutdown();
			}
		}

	}

}
