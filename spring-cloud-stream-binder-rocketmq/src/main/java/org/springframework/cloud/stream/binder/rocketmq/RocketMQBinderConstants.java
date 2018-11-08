package org.springframework.cloud.stream.binder.rocketmq;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public interface RocketMQBinderConstants {

    /**
     * Header key
     */
    String ORIGINAL_ROCKET_MESSAGE = "ORIGINAL_ROCKET_MESSAGE";

    String ROCKET_FLAG = "ROCKET_FLAG";

    String ROCKET_SEND_RESULT = "ROCKET_SEND_RESULT";

    String ACKNOWLEDGEMENT_KEY = "ACKNOWLEDGEMENT";

    /**
     * Instrumentation key
     */
    String LASTSEND_TIMESTAMP = "lastSend.timestamp";

    String ENDPOINT_ID = "rocketmq-binder";

}
