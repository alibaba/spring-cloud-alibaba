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

package org.springframework.cloud.stream.binder.rocketmq.metrics;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.MQClientManager;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.cloud.stream.binder.BindingCreatedEvent;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQMessageChannelBinder;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import org.springframework.context.ApplicationListener;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQBinderMetrics
		implements MeterBinder, ApplicationListener<BindingCreatedEvent> {

	private final RocketMQMessageChannelBinder rocketMQMessageChannelBinder;
	private final RocketMQBinderConfigurationProperties rocketMQBinderConfigurationProperties;
	private final MeterRegistry meterRegistry;

	static final String METRIC_NAME = "spring.cloud.stream.binder.rocketmq";

	public RocketMQBinderMetrics(
			RocketMQMessageChannelBinder rocketMQMessageChannelBinder,
			RocketMQBinderConfigurationProperties rocketMQBinderConfigurationProperties,
			MeterRegistry meterRegistry) {
		this.rocketMQMessageChannelBinder = rocketMQMessageChannelBinder;
		this.rocketMQBinderConfigurationProperties = rocketMQBinderConfigurationProperties;
		this.meterRegistry = meterRegistry;
	}

	@Override
	public void bindTo(@NonNull MeterRegistry registry) {
		DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer();
		pushConsumer
				.setNamesrvAddr(rocketMQBinderConfigurationProperties.getNameServer());
		DefaultMQProducer producer = new DefaultMQProducer();
		producer.setNamesrvAddr(rocketMQBinderConfigurationProperties.getNameServer());

		rocketMQMessageChannelBinder.getTopicInUse().forEach((topic, group) -> {
			Gauge.builder(METRIC_NAME, this, o -> calculateMsgQueueOffset(topic, group))
					.tag("group", group).tag("topic", topic)
					.description("RocketMQ all messageQueue size").register(registry);
		});

	}

	private double calculateMsgQueueOffset(String topic, String group) {
		for (String clientConfigId : this.rocketMQMessageChannelBinder
				.getClientConfigId()) {
			ClientConfig clientConfig = new ClientConfig();
			String[] clientConfigArr = clientConfigId.split("@", 3);
			clientConfig.setClientIP(clientConfigArr[0]);
			clientConfig.setInstanceName(clientConfigArr[1]);
			if (clientConfigArr.length > 2) {
				clientConfig.setUnitName(clientConfigArr[2]);
			}
			Map<MessageQueue, Long> queueLongMap = MQClientManager.getInstance()
					.getAndCreateMQClientInstance(clientConfig)
					.getConsumerStatus(topic, group);
			if (queueLongMap.size() == 0) {
				continue;
			}
			return queueLongMap.values().stream()
					.collect(Collectors.summingLong(Long::longValue));
		}
		return 0.0;
	}

	@Override
	public void onApplicationEvent(BindingCreatedEvent event) {
		if (this.meterRegistry != null) {
			this.bindTo(this.meterRegistry);
		}
	}
}
