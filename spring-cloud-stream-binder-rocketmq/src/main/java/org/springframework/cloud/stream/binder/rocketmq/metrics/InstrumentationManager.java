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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants.Metrics.Consumer;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants.Metrics.Producer;

import com.codahale.metrics.MetricRegistry;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class InstrumentationManager {

	private final MetricRegistry metricRegistry = new MetricRegistry();
	private final Map<String, Object> runtime = new ConcurrentHashMap<>();

	private final Map<String, ProducerInstrumentation> producerInstrumentations = new HashMap<>();
	private final Map<String, ConsumerInstrumentation> consumeInstrumentations = new HashMap<>();
	private final Map<String, ConsumerGroupInstrumentation> consumerGroupsInstrumentations = new HashMap<>();

	private final Map<String, Instrumentation> healthInstrumentations = new HashMap<>();

	public ProducerInstrumentation getProducerInstrumentation(String destination) {
		String key = Producer.PREFIX + destination;
		producerInstrumentations.putIfAbsent(key,
				new ProducerInstrumentation(metricRegistry, key));
		return producerInstrumentations.get(key);
	}

	public ConsumerInstrumentation getConsumerInstrumentation(String destination) {
		String key = Consumer.PREFIX + destination;
		consumeInstrumentations.putIfAbsent(key,
				new ConsumerInstrumentation(metricRegistry, key));
		return consumeInstrumentations.get(key);
	}

	public ConsumerGroupInstrumentation getConsumerGroupInstrumentation(String group) {
		String key = Consumer.GROUP_PREFIX + group;
		consumerGroupsInstrumentations.putIfAbsent(key,
				new ConsumerGroupInstrumentation(metricRegistry, key));
		return consumerGroupsInstrumentations.get(key);
	}

	public Set<Instrumentation> getHealthInstrumentations() {
		return healthInstrumentations.entrySet().stream().map(Map.Entry::getValue)
				.collect(Collectors.toSet());
	}

	public void addHealthInstrumentation(Instrumentation instrumentation) {
		healthInstrumentations.put(instrumentation.getName(), instrumentation);
	}

	public Map<String, Object> getRuntime() {
		return runtime;
	}

	public MetricRegistry getMetricRegistry() {
		return metricRegistry;
	}
}
