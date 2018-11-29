package org.springframework.cloud.stream.binder.rocketmq.actuator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.binder.rocketmq.metrics.Instrumentation;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQBinderHealthIndicator extends AbstractHealthIndicator {

	@Autowired(required = false)
	private InstrumentationManager instrumentationManager;

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		if (instrumentationManager != null) {
			if (instrumentationManager.getHealthInstrumentations().stream()
					.allMatch(Instrumentation::isUp)) {
				builder.up();
				return;
			}
			if (instrumentationManager.getHealthInstrumentations().stream()
					.allMatch(Instrumentation::isOutOfService)) {
				builder.outOfService();
				return;
			}
			builder.down();
			instrumentationManager.getHealthInstrumentations().stream()
					.filter(instrumentation -> !instrumentation.isStarted())
					.forEach(instrumentation1 -> builder
							.withException(instrumentation1.getStartException()));
		}
		else {
			builder.down();
			builder.withDetail("warning",
					"please add metrics-core dependency, we use it for metrics");
		}

	}
}
