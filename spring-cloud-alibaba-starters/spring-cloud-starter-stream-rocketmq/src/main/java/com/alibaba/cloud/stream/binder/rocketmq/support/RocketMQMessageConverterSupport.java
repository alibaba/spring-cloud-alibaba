/*
 * Copyright 2013-2023 the original author or authors.
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

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import com.alibaba.cloud.stream.binder.rocketmq.constant.RocketMQConst;
import com.alibaba.cloud.stream.binder.rocketmq.constant.RocketMQConst.Headers;
import com.alibaba.cloud.stream.binder.rocketmq.convert.RocketMQMessageConverter;
import com.alibaba.cloud.stream.binder.rocketmq.custom.RocketMQBeanContainerCache;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author zkzlx
 */
public final class RocketMQMessageConverterSupport {

	private RocketMQMessageConverterSupport() {
	}

	private static final CompositeMessageConverter MESSAGE_CONVERTER = RocketMQBeanContainerCache
			.getBean(RocketMQMessageConverter.DEFAULT_NAME,
					CompositeMessageConverter.class,
					new RocketMQMessageConverter().getMessageConverter());

	public static Message convertMessage2Spring(MessageExt message) {
		MessageBuilder messageBuilder = MessageBuilder.withPayload(message.getBody())
				.setHeader(toRocketHeaderKey(Headers.KEYS), message.getKeys())
				.setHeader(toRocketHeaderKey(Headers.TAGS), message.getTags())
				.setHeader(toRocketHeaderKey(Headers.TOPIC), message.getTopic())
				.setHeader(toRocketHeaderKey(Headers.MESSAGE_ID), message.getMsgId())
				.setHeader(toRocketHeaderKey(Headers.BORN_TIMESTAMP),
						message.getBornTimestamp())
				.setHeader(toRocketHeaderKey(Headers.BORN_HOST),
						message.getBornHostString())
				.setHeader(toRocketHeaderKey(Headers.FLAG), message.getFlag())
				.setHeader(toRocketHeaderKey(Headers.QUEUE_ID), message.getQueueId())
				.setHeader(toRocketHeaderKey(Headers.SYS_FLAG), message.getSysFlag())
				.setHeader(toRocketHeaderKey(Headers.TRANSACTION_ID),
						message.getTransactionId());
		addUserProperties(message.getProperties(), messageBuilder);
		return messageBuilder.build();
	}

	public static String toRocketHeaderKey(String rawKey) {
		return "ROCKET_" + rawKey;
	}

	private static void addUserProperties(Map<String, String> properties,
			MessageBuilder messageBuilder) {
		if (!CollectionUtils.isEmpty(properties)) {
			properties.forEach((key, val) -> {
				if (!MessageConst.STRING_HASH_SET.contains(key)
						&& !MessageHeaders.ID.equals(key)
						&& !MessageHeaders.TIMESTAMP.equals(key)) {
					messageBuilder.setHeader(key, val);
				}
			});
		}
	}

	public static org.apache.rocketmq.common.message.Message convertMessage2MQ(
			String destination, Message<?> source) {
		Message<?> message = MESSAGE_CONVERTER.toMessage(source.getPayload(),
				source.getHeaders());
		assert message != null;
		MessageBuilder<?> builder = MessageBuilder.fromMessage(message);
		builder.setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN);
		message = builder.build();
		return doConvert(destination, message);
	}

	private static org.apache.rocketmq.common.message.Message doConvert(String topic,
			Message<?> message) {
		Charset charset = Charset.defaultCharset();
		Object payloadObj = message.getPayload();
		byte[] payloads;
		try {
			if (payloadObj instanceof String) {
				payloads = ((String) payloadObj).getBytes(charset);
			}
			else if (payloadObj instanceof byte[]) {
				payloads = (byte[]) message.getPayload();
			}
			else {
				String jsonObj = (String) MESSAGE_CONVERTER.fromMessage(message,
						payloadObj.getClass());
				if (null == jsonObj) {
					throw new RuntimeException(String.format(
							"empty after conversion [messageConverter:%s,payloadClass:%s,payloadObj:%s]",
							MESSAGE_CONVERTER.getClass(), payloadObj.getClass(),
							payloadObj));
				}
				payloads = jsonObj.getBytes(charset);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("convert to RocketMQ message failed.", e);
		}
		return getAndWrapMessage(topic, message.getHeaders(), payloads);
	}

	private static org.apache.rocketmq.common.message.Message getAndWrapMessage(
			String topic, MessageHeaders headers, byte[] payloads) {
		if (topic == null || topic.length() < 1) {
			return null;
		}
		if (payloads == null || payloads.length < 1) {
			return null;
		}
		org.apache.rocketmq.common.message.Message rocketMsg = new org.apache.rocketmq.common.message.Message(
				topic, payloads);
		if (Objects.nonNull(headers) && !headers.isEmpty()) {
			Object tag = headers.getOrDefault(Headers.TAGS,
					headers.get(toRocketHeaderKey(Headers.TAGS)));
			if (!ObjectUtils.isEmpty(tag)) {
				rocketMsg.setTags(String.valueOf(tag));
			}

			Object keys = headers.getOrDefault(Headers.KEYS,
					headers.get(toRocketHeaderKey(Headers.KEYS)));
			if (!ObjectUtils.isEmpty(keys)) {
				rocketMsg.setKeys(keys.toString());
			}
			Object flagObj = headers.getOrDefault(Headers.FLAG,
					headers.get(toRocketHeaderKey(Headers.FLAG)));
			int flag = 0;
			int delayLevel = 0;
			try {
				flagObj = flagObj == null ? 0 : flagObj;
				Object delayLevelObj = headers.getOrDefault(
						RocketMQConst.PROPERTY_DELAY_TIME_LEVEL,
						headers.get(toRocketHeaderKey(
								RocketMQConst.PROPERTY_DELAY_TIME_LEVEL)));
				delayLevelObj = delayLevelObj == null ? 0 : delayLevelObj;
				delayLevel = Integer.parseInt(String.valueOf(delayLevelObj));
				flag = Integer.parseInt(String.valueOf(flagObj));
			}
			catch (Exception ignored) {
			}
			if (delayLevel > 0) {
				rocketMsg.setDelayTimeLevel(delayLevel);
			}
			rocketMsg.setFlag(flag);
			Object waitStoreMsgOkObj = headers
					.getOrDefault(RocketMQConst.PROPERTY_WAIT_STORE_MSG_OK, "true");
			rocketMsg.setWaitStoreMsgOK(
					Boolean.parseBoolean(String.valueOf(waitStoreMsgOkObj)));
			headers.entrySet().stream()
					.filter(entry -> !Objects.equals(entry.getKey(), Headers.FLAG))
					.forEach(entry -> {
						if (!MessageConst.STRING_HASH_SET.contains(entry.getKey())) {
							String val = String.valueOf(entry.getValue());
							// Remove All blank header(rocketmq not support).
							if (org.apache.commons.lang3.StringUtils.isNotBlank(val)) {
								rocketMsg.putUserProperty(entry.getKey(), val);
							}
						}
					});

		}
		return rocketMsg;
	}

}
