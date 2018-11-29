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
