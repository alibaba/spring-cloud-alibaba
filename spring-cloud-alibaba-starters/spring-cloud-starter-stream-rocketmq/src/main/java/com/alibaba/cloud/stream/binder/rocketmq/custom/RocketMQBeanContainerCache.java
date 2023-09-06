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

package com.alibaba.cloud.stream.binder.rocketmq.custom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.stream.binder.rocketmq.extend.ErrorAcknowledgeHandler;
import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.hook.CheckForbiddenHook;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.TransactionListener;

import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.util.StringUtils;

/**
 * Gets the beans configured in the configuration file.
 *
 * @author junboXiang
 */
public final class RocketMQBeanContainerCache {

	private RocketMQBeanContainerCache() {
	}

	private static final Class<?>[] CLASSES = new Class[] {
			CompositeMessageConverter.class, AllocateMessageQueueStrategy.class,
			MessageQueueSelector.class, MessageListener.class, TransactionListener.class,
			SendCallback.class, CheckForbiddenHook.class, SendMessageHook.class,
			ErrorAcknowledgeHandler.class };

	private static final Map<String, Object> BEANS_CACHE = new ConcurrentHashMap<>();

	static void putBean(String beanName, Object beanObj) {
		BEANS_CACHE.put(beanName, beanObj);
	}

	static Class<?>[] getClassAry() {
		return CLASSES;
	}

	public static <T> T getBean(String beanName, Class<T> clazz) {
		return getBean(beanName, clazz, null);
	}

	public static <T> T getBean(String beanName, Class<T> clazz, T defaultObj) {
		if (StringUtils.isEmpty(beanName)) {
			return defaultObj;
		}
		Object obj = BEANS_CACHE.get(beanName);
		if (null == obj) {
			return defaultObj;
		}
		if (clazz.isAssignableFrom(obj.getClass())) {
			return (T) obj;
		}
		return defaultObj;
	}

}
