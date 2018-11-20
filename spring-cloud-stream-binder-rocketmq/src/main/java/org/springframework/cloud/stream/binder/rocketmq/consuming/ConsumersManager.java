package org.springframework.cloud.stream.binder.rocketmq.consuming;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
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
	private final InstrumentationManager instrumentationManager;
	private final RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties;

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
		ConsumerGroupInstrumentation instrumentation = instrumentationManager
				.getConsumerGroupInstrumentation(group);
		instrumentationManager.addHealthInstrumentation(instrumentation);

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
		ConsumerGroupInstrumentation groupInstrumentation = instrumentationManager
				.getConsumerGroupInstrumentation(group);
		instrumentationManager.addHealthInstrumentation(groupInstrumentation);
		try {
			consumerGroups.get(group).start();
			started.put(group, true);
			groupInstrumentation.markStartedSuccessfully();
		}
		catch (MQClientException e) {
			groupInstrumentation.markStartFailed(e);
			logger.error("RocketMQ Consumer hasn't been started. Caused by "
					+ e.getErrorMessage(), e);
			throw e;
		}
	}

	public synchronized Set<String> getConsumerGroups() {
		return consumerGroups.keySet();
	}
}
