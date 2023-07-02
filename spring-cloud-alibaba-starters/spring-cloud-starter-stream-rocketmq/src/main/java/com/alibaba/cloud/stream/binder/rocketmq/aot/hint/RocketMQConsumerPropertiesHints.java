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

package com.alibaba.cloud.stream.binder.rocketmq.aot.hint;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.util.ReflectionUtils;

/**
 * @author ChengPu raozihao
 */
public class RocketMQConsumerPropertiesHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		Constructor<RocketMQConsumerProperties> constructor;
		try {
			constructor = RocketMQConsumerProperties.class.getConstructor();
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		hints.reflection().registerConstructor(constructor, ExecutableMode.INVOKE);
		// setMessageModel
		Method setMessageModel = ReflectionUtils.findMethod(RocketMQConsumerProperties.class, "setMessageModel", String.class);
		hints.reflection().registerMethod(setMessageModel, ExecutableMode.INVOKE);
		// getPush
		Method getPush = ReflectionUtils.findMethod(RocketMQConsumerProperties.class, "getPush");
		hints.reflection().registerMethod(getPush, ExecutableMode.INVOKE);
		// setSubscription
		Method setSubscription = ReflectionUtils.findMethod(RocketMQConsumerProperties.class, "setSubscription", String.class);
		hints.reflection().registerMethod(setSubscription, ExecutableMode.INVOKE);
	}
}
