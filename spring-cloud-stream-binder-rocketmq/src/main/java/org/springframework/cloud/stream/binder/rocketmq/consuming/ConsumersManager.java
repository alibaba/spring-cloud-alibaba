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

package org.springframework.cloud.stream.binder.rocketmq.consuming;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.rocketmq.metrics.ConsumerGroupInstrumentation;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ConsumersManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, DefaultMQPushConsumer> consumerGroups = new HashMap<>();
	private final Map<String, Boolean> started = new HashMap<>();
	private final Map<Map.Entry<String, String>, ExtendedConsumerProperties<RocketMQConsumerProperties>> propertiesMap = new HashMap<>();
	private final RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties;

	private InstrumentationManager instrumentationManager;

	public ConsumersManager(InstrumentationManager instrumentationManager,
			RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties) {
		this.instrumentationManager = instrumentationManager;
		this.rocketBinderConfigurationProperties = rocketBinderConfigurationProperties;
	}

	public synchronized DefaultMQPushConsumer getOrCreateConsumer(String group,
			String topic,
			ExtendedConsumerProperties<RocketMQConsumerProperties> consumerProperties) {
		propertiesMap.put(new AbstractMap.SimpleEntry<>(group, topic),
				consumerProperties);

		Optional.ofNullable(instrumentationManager).ifPresent(manager -> {
			ConsumerGroupInstrumentation instrumentation = manager
					.getConsumerGroupInstrumentation(group);
			instrumentationManager.addHealthInstrumentation(instrumentation);
		});

		if (consumerGroups.containsKey(group)) {
			return consumerGroups.get(group);
		}

		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(group);
		consumer.setNamesrvAddr(rocketBinderConfigurationProperties.getNamesrvAddr());
		consumerGroups.put(group, consumer);
		started.put(group, false);
		consumer.setConsumeThreadMax(consumerProperties.getConcurrency());
		consumer.setConsumeThreadMin(consumerProperties.getConcurrency());
		if (consumerProperties.getExtension().getBroadcasting()) {
			consumer.setMessageModel(MessageModel.BROADCASTING);
		}
		logger.info("RocketMQ consuming for SCS group {} created", group);
		return consumer;
	}

	public synchronized void startConsumers() throws MQClientException {
		for (String group : getConsumerGroups()) {
			start(group);
		}
	}

	public synchronized void startConsumer(String group) throws MQClientException {
		start(group);
	}

	public synchronized void stopConsumer(String group) {
		stop(group);
	}

	private void stop(String group) {
		if (consumerGroups.get(group) != null) {
			consumerGroups.get(group).shutdown();
			started.put(group, false);
		}
	}

	private synchronized void start(String group) throws MQClientException {
		if (started.get(group)) {
			return;
		}

		ConsumerGroupInstrumentation groupInstrumentation = null;
		if (Optional.ofNullable(instrumentationManager).isPresent()) {
			groupInstrumentation = instrumentationManager
					.getConsumerGroupInstrumentation(group);
			instrumentationManager.addHealthInstrumentation(groupInstrumentation);
		}

		try {
			consumerGroups.get(group).start();
			started.put(group, true);
			Optional.ofNullable(groupInstrumentation)
					.ifPresent(g -> g.markStartedSuccessfully());
		}
		catch (MQClientException e) {
			Optional.ofNullable(groupInstrumentation)
					.ifPresent(g -> g.markStartFailed(e));
			logger.error("RocketMQ Consumer hasn't been started. Caused by "
					+ e.getErrorMessage(), e);
			throw e;
		}
	}

	public synchronized Set<String> getConsumerGroups() {
		return consumerGroups.keySet();
	}
}
