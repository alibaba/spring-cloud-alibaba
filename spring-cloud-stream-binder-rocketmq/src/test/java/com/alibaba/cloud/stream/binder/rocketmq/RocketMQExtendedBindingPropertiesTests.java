/*
 * Copyright 2016-2017 the original author or authors.
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

package com.alibaba.cloud.stream.binder.rocketmq;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.MapPropertySource;

import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQExtendedBindingProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;

/**
 * {@link RocketMQExtendedBindingProperties} unit test class
 *
 * @author <a href="mailto:jiashuai.xie01@gmail.com">Xiejiashuai</a>
 */
public class RocketMQExtendedBindingPropertiesTests {

	private RocketMQExtendedBindingProperties properties;

	@Before
	public void before() {

		// initialize test application context
		StaticApplicationContext context = new StaticApplicationContext();

		// add internal properties
		Map<String, Object> configMap = new HashMap<>();
		configMap.put(
				"spring.cloud.stream.rocketmq.bindings.output.producer.maxMessageSize",
				"12345");
		configMap.put(
				"spring.cloud.stream.rocketmq.bindings.output.producer.sendMessageTimeout",
				"100");
		configMap.put(
				"spring.cloud.stream.rocketmq.bindings.output.producer.compressMessageBodyThreshold",
				"1111");
		configMap.put("spring.cloud.stream.rocketmq.bindings.input.consumer.tags",
				"test");
		configMap.put(
				"spring.cloud.stream.rocketmq.bindings.input.consumer.delayLevelWhenNextConsume",
				"100");
		configMap.put("spring.cloud.stream.rocketmq.bindings.input.consumer.enabled",
				"false");

		context.getEnvironment().getPropertySources().addLast(
				new MapPropertySource("rocketMQExtendedBindingProperties", configMap));

		context.registerSingleton(ConfigurationBeanFactoryMetadata.BEAN_NAME,
				ConfigurationBeanFactoryMetadata.class);

		context.registerSingleton(ConfigurationPropertiesBindingPostProcessor.BEAN_NAME,
				ConfigurationPropertiesBindingPostProcessor.class);

		// register singleton bean
		context.registerSingleton("rocketMQExtendedBindingProperties",
				RocketMQExtendedBindingProperties.class);

		// refresh test application context
		context.refresh();

		properties = context.getBean(RocketMQExtendedBindingProperties.class);
	}

	@Test
	public void testRocketMQProducerProperties() {

		RocketMQProducerProperties producerProperties = properties
				.getExtendedProducerProperties("output");

		assertThat(producerProperties.getSendMessageTimeout() == 100).isTrue();
		assertThat(producerProperties.getSendMessageTimeout() == 3000).isFalse();
		assertThat(producerProperties.getMaxMessageSize().equals(12345)).isTrue();
		assertThat(producerProperties.getMaxMessageSize().equals(1024 * 1024 * 4))
				.isFalse();
		assertThat(producerProperties.getCompressMessageBodyThreshold() == 1111).isTrue();
		assertThat(
				producerProperties.getCompressMessageBodyThreshold() == 1024 * 1024 * 4)
						.isFalse();
	}

	@Test
	public void testRocketMQConsumerProperties() {
		RocketMQConsumerProperties consumerProperties = properties
				.getExtendedConsumerProperties("input");
		assertThat(consumerProperties.getTags().equals("test")).isTrue();
		assertThat(consumerProperties.getDelayLevelWhenNextConsume() == 100).isTrue();
		assertThat(consumerProperties.getDelayLevelWhenNextConsume() == 0).isFalse();
		assertThat(consumerProperties.getEnabled().equals(Boolean.FALSE)).isTrue();
		assertThat(consumerProperties.getEnabled().equals(Boolean.TRUE)).isFalse();
	}

}
