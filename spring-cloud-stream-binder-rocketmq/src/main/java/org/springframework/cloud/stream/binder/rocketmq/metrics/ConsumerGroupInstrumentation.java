package org.springframework.cloud.stream.binder.rocketmq.metrics;

import com.codahale.metrics.MetricRegistry;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ConsumerGroupInstrumentation extends Instrumentation {
	private MetricRegistry metricRegistry;

	public ConsumerGroupInstrumentation(MetricRegistry metricRegistry, String name) {
		super(name);
		this.metricRegistry = metricRegistry;
	}

}
