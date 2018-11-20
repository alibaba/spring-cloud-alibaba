package org.springframework.cloud.stream.binder.rocketmq.metrics;

import java.util.concurrent.atomic.AtomicBoolean;

import com.codahale.metrics.MetricRegistry;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ConsumerGroupInstrumentation extends Instrumentation {
	private MetricRegistry metricRegistry;

	private AtomicBoolean delayedStart = new AtomicBoolean(false);

	public ConsumerGroupInstrumentation(MetricRegistry metricRegistry, String name) {
		super(name);
		this.metricRegistry = metricRegistry;
	}

	public void markDelayedStart() {
		delayedStart.set(true);
	}

	@Override
	public boolean isUp() {
		return started.get() || delayedStart.get();
	}

	@Override
	public boolean isOutOfService() {
		return !started.get() && startException == null && !delayedStart.get();
	}
}
