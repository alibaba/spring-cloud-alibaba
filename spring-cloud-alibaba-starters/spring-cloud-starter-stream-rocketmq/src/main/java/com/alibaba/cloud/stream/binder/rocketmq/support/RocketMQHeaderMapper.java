/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.stream.binder.rocketmq.support;

import java.util.Map;

import org.springframework.messaging.MessageHeaders;

/**
 * header value mapper for RocketMQ.
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
