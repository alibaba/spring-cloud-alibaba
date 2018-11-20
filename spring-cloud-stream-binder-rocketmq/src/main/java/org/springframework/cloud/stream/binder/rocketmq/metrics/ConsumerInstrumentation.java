package org.springframework.cloud.stream.binder.rocketmq.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**
 * @author juven.xuxb
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ConsumerInstrumentation extends Instrumentation {

	private final Counter totalConsumed;
	private final Counter totalConsumedFailures;
	private final Meter consumedPerSecond;
	private final Meter consumedFailuresPerSecond;

	public ConsumerInstrumentation(MetricRegistry registry, String baseMetricName) {
		super(baseMetricName);
		this.totalConsumed = registry.counter(name(baseMetricName, "totalConsumed"));
		this.consumedPerSecond = registry
				.meter(name(baseMetricName, "consumedPerSecond"));
		this.totalConsumedFailures = registry
				.counter(name(baseMetricName, "totalConsumedFailures"));
		this.consumedFailuresPerSecond = registry
				.meter(name(baseMetricName, "consumedFailuresPerSecond"));
	}

	public void markConsumed() {
		totalConsumed.inc();
		consumedPerSecond.mark();
	}

	public void markConsumedFailure() {
		totalConsumedFailures.inc();
		consumedFailuresPerSecond.mark();
	}
}
