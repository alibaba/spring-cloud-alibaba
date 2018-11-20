package org.springframework.cloud.stream.binder.rocketmq.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.codahale.metrics.MetricRegistry;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class InstrumentationManager {
	private final MetricRegistry metricRegistry;
	private final Map<String, Object> runtime;
	private final Map<String, ProducerInstrumentation> producerInstrumentations = new HashMap<>();
	private final Map<String, ConsumerInstrumentation> consumeInstrumentations = new HashMap<>();
	private final Map<String, ConsumerGroupInstrumentation> consumerGroupsInstrumentations = new HashMap<>();

	private final Map<String, Instrumentation> healthInstrumentations = new HashMap<>();

	public InstrumentationManager(MetricRegistry metricRegistry,
			Map<String, Object> runtime) {
		this.metricRegistry = metricRegistry;
		this.runtime = runtime;
	}

	public ProducerInstrumentation getProducerInstrumentation(String destination) {
		String key = "scs-rocketmq.producer." + destination;
		producerInstrumentations.putIfAbsent(key,
				new ProducerInstrumentation(metricRegistry, key));
		return producerInstrumentations.get(key);
	}

	public ConsumerInstrumentation getConsumerInstrumentation(String destination) {
		String key = "scs-rocketmq.consumer." + destination;
		consumeInstrumentations.putIfAbsent(key,
				new ConsumerInstrumentation(metricRegistry, key));
		return consumeInstrumentations.get(key);
	}

	public ConsumerGroupInstrumentation getConsumerGroupInstrumentation(String group) {
		String key = "scs-rocketmq.consumerGroup." + group;
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
}
