package com.alibaba.cloud.stream.binder.rocketmq.support;

import java.util.Map;

import org.springframework.messaging.MessageHeaders;

/**
 * header value mapper for RocketMQ
 *
 * @author caotc
 * @since 2.1.1.RELEASE
 */
public interface RocketMQHeaderMapper {
	/**
	 * Map from the given {@link MessageHeaders} to the specified target message.
	 * @param headers the abstracted MessageHeaders.
	 * @return the native target message.
	 */
	Map<String, String> fromHeaders(MessageHeaders headers);

	/**
	 * Map from the given target message to abstracted {@link MessageHeaders}.
	 * @param source the native target message.
	 * @return the target headers.
	 */
	MessageHeaders toHeaders(Map<String, String> source);
}
