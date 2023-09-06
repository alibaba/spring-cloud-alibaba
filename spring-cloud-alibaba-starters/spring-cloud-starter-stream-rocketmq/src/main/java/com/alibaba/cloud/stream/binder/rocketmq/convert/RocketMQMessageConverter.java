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

package com.alibaba.cloud.stream.binder.rocketmq.convert;

import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.util.ClassUtils;

/**
 * The default message converter of rocketMq,its bean name is {@link #DEFAULT_NAME} .
 *
 * @author zkzlx
 */
public class RocketMQMessageConverter extends AbstractMessageConverter {

	/**
	 * if you want to customize a bean, please use the BeanName.
	 */
	public static final String DEFAULT_NAME = "com.alibaba.cloud.stream.binder.rocketmq.convert.RocketMQMessageConverter";

	private static final boolean JACKSON_PRESENT;

	private static final boolean FASTJSON_PRESENT;

	static {
		ClassLoader classLoader = RocketMQMessageConverter.class.getClassLoader();
		JACKSON_PRESENT = ClassUtils
				.isPresent("com.fasterxml.jackson.databind.ObjectMapper", classLoader)
				&& ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator",
						classLoader);
		FASTJSON_PRESENT = ClassUtils.isPresent("com.alibaba.fastjson.JSON", classLoader)
				&& ClassUtils.isPresent(
						"com.alibaba.fastjson.support.config.FastJsonConfig",
						classLoader);
	}

	private CompositeMessageConverter messageConverter;

	public RocketMQMessageConverter() {
		List<MessageConverter> messageConverters = new ArrayList<>();
		ByteArrayMessageConverter byteArrayMessageConverter = new ByteArrayMessageConverter();
		byteArrayMessageConverter.setContentTypeResolver(null);
		messageConverters.add(byteArrayMessageConverter);
		messageConverters.add(new StringMessageConverter());
		if (JACKSON_PRESENT) {
			messageConverters.add(new MappingJackson2MessageConverter());
		}
		if (FASTJSON_PRESENT) {
			try {
				messageConverters.add((MessageConverter) ClassUtils.forName(
						"com.alibaba.fastjson.support.spring.messaging.MappingFastJsonMessageConverter",
						ClassUtils.getDefaultClassLoader()).newInstance());
			}
			catch (ClassNotFoundException | IllegalAccessException
					| InstantiationException ignored) {
				// ignore this exception
			}
		}
		messageConverter = new CompositeMessageConverter(messageConverters);
	}

	public CompositeMessageConverter getMessageConverter() {
		return messageConverter;
	}

	public void setMessageConverter(CompositeMessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}

	/**
	 * support all classes.
	 * @param clazz classes.
	 * @return awayls true.
	 */
	@Override
	protected boolean supports(Class<?> clazz) {
		return true;
	}

	/**
	 * Convert the message payload from serialized form to an Object by
	 * RocketMQMessageConverter.
	 * @param message the input message
	 * @param targetClass the target class for the conversion
	 * @param conversionHint an extra object passed to the {@link MessageConverter}, e.g.
	 * the associated {@code MethodParameter} (may be {@code null}}
	 * @return the result of the conversion, or {@code null} if the converter cannot
	 * perform the conversion
	 * @since 4.2
	 */
	@Override
	protected Object convertFromInternal(Message<?> message, Class<?> targetClass,
			Object conversionHint) {
		Object payload = null;
		for (MessageConverter converter : getMessageConverter().getConverters()) {
			try {
				payload = converter.fromMessage(message, targetClass);
			}
			catch (Exception ignore) {
			}
			if (payload != null) {
				return payload;
			}
		}
		if (payload == null && logger.isDebugEnabled()) {
			logger.debug("Can convert message " + message.toString());
		}
		return payload;
	}

}
