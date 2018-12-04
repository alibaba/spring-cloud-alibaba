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
