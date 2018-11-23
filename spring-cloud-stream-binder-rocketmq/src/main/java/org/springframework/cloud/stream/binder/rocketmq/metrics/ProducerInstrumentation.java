package org.springframework.cloud.stream.binder.rocketmq.metrics;

import static com.codahale.metrics.MetricRegistry.name;

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
		this.totalSent = registry.counter(name(baseMetricName, "totalSent"));
		this.totalSentFailures = registry
				.counter(name(baseMetricName, "totalSentFailures"));
		this.sentPerSecond = registry.meter(name(baseMetricName, "sentPerSecond"));
		this.sentFailuresPerSecond = registry
				.meter(name(baseMetricName, "sentFailuresPerSecond"));
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
