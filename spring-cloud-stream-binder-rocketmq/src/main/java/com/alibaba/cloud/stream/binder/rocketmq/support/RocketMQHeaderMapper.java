package com.alibaba.cloud.stream.binder.rocketmq.support;

import org.springframework.messaging.MessageHeaders;

import java.util.Map;

/**
* header value mapper for RocketMQ
*
* @author caotc
* @date 2019-08-22
* @since 2.1.1
*/
public interface RocketMQHeaderMapper {
    /**
     * Map from the given {@link MessageHeaders} to the specified target message.
     * @param headers the abstracted MessageHeaders.
     * @return  the native target message.
     */
    Map<String,String> fromHeaders(MessageHeaders headers);
    /**
     * Map from the given target message to abstracted {@link MessageHeaders}.
     * @param source the native target message.
     * @return  the target headers.
     */
    MessageHeaders toHeaders(Map<String,String> source);
}
