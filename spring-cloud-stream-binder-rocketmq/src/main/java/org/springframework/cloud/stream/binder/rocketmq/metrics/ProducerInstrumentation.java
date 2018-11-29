package org.springframework.cloud.stream.binder.rocketmq.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants.Metrics.Producer;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**
 * @author juven.xuxb
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ProducerInstrumentation extends Instrumentation {

	private final Counter totalSent;
	private final Counter totalSentFailures;
	private final Meter sentPerSecond;
	private final Meter sentFailuresPerSecond;

	public ProducerInstrumentation(MetricRegistry registry, String baseMetricName) {
		super(baseMetricName);

		this.totalSent = registry.counter(name(baseMetricName, Producer.TOTAL_SENT));
		this.totalSentFailures = registry
				.counter(name(baseMetricName, Producer.TOTAL_SENT_FAILURES));
		this.sentPerSecond = registry
				.meter(name(baseMetricName, Producer.SENT_PER_SECOND));
		this.sentFailuresPerSecond = registry
				.meter(name(baseMetricName, Producer.SENT_FAILURES_PER_SECOND));
	}

	public void markSent() {
		totalSent.inc();
		sentPerSecond.mark();
	}

	public void markSentFailure() {
		totalSentFailures.inc();
		sentFailuresPerSecond.mark();
	}
}
