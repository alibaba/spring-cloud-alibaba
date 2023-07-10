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
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQSpecificPropertiesProvider;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.util.ReflectionUtils;

/**
 * @author ChengPu raozihao
 */
public class RocketMQSpecificPropertiesProviderHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		Constructor<RocketMQSpecificPropertiesProvider> constructor;
		try {
			constructor = RocketMQSpecificPropertiesProvider.class.getConstructor();
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		hints.reflection().registerConstructor(constructor, ExecutableMode.INVOKE);
		// getConsumer
		Method getConsumer = ReflectionUtils.findMethod(RocketMQSpecificPropertiesProvider.class, "getConsumer");
		hints.reflection().registerMethod(getConsumer, ExecutableMode.INVOKE);
		// setConsumer
		Method setConsumer = ReflectionUtils.findMethod(RocketMQSpecificPropertiesProvider.class, "setConsumer", RocketMQConsumerProperties.class);
		hints.reflection().registerMethod(setConsumer, ExecutableMode.INVOKE);
		// getProducer
		Method getProducer = ReflectionUtils.findMethod(RocketMQSpecificPropertiesProvider.class, "getProducer");
		hints.reflection().registerMethod(getProducer, ExecutableMode.INVOKE);
		// setProducer
		Method setProducer = ReflectionUtils.findMethod(RocketMQSpecificPropertiesProvider.class, "setProducer", RocketMQProducerProperties.class);
		hints.reflection().registerMethod(setProducer, ExecutableMode.INVOKE);
	}
}
