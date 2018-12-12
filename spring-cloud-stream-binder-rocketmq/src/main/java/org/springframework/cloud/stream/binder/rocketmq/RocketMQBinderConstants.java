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

package org.springframework.cloud.stream.binder.rocketmq;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public interface RocketMQBinderConstants {

	String ENDPOINT_ID = "rocketmq-binder";

	/**
	 * Header key
	 */
	String ORIGINAL_ROCKET_MESSAGE = "ORIGINAL_ROCKETMQ_MESSAGE";

	String ROCKET_FLAG = "ROCKETMQ_FLAG";

	String ROCKET_SEND_RESULT = "ROCKETMQ_SEND_RESULT";

	String ROCKET_TRANSACTIONAL_ARG = "ROCKETMQ_TRANSACTIONAL_ARG";

	String ACKNOWLEDGEMENT_KEY = "ACKNOWLEDGEMENT";

	/**
	 * Instrumentation
	 */
	String LASTSEND_TIMESTAMP = "lastSend.timestamp";

	interface Metrics {
		interface Producer {
			String PREFIX = "scs-rocketmq.producer.";
			String TOTAL_SENT = "totalSent";
			String TOTAL_SENT_FAILURES = "totalSentFailures";
			String SENT_PER_SECOND = "sentPerSecond";
			String SENT_FAILURES_PER_SECOND = "sentFailuresPerSecond";
		}

		interface Consumer {
			String GROUP_PREFIX = "scs-rocketmq.consumerGroup.";
			String PREFIX = "scs-rocketmq.consumer.";
			String TOTAL_CONSUMED = "totalConsumed";
			String CONSUMED_PER_SECOND = "consumedPerSecond";
			String TOTAL_CONSUMED_FAILURES = "totalConsumedFailures";
			String CONSUMED_FAILURES_PER_SECOND = "consumedFailuresPerSecond";
		}
	}

}
