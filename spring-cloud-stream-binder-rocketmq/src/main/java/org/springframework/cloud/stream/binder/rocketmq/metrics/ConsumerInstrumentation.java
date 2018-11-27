package org.springframework.cloud.stream.binder.rocketmq.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants.Metrics.Consumer;

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

		this.totalConsumed = registry
				.counter(name(baseMetricName, Consumer.TOTAL_CONSUMED));
		this.consumedPerSecond = registry
				.meter(name(baseMetricName, Consumer.CONSUMED_PER_SECOND));
		this.totalConsumedFailures = registry
				.counter(name(baseMetricName, Consumer.TOTAL_CONSUMED_FAILURES));
		this.consumedFailuresPerSecond = registry
				.meter(name(baseMetricName, Consumer.CONSUMED_FAILURES_PER_SECOND));
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
