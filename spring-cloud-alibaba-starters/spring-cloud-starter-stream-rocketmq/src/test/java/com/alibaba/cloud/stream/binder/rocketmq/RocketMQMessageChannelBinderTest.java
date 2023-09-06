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

package com.alibaba.cloud.stream.binder.rocketmq;

import javax.annotation.Resource;

import com.alibaba.cloud.stream.binder.rocketmq.autoconfigurate.ExtendedBindingHandlerMappingsProviderConfiguration;
import com.alibaba.cloud.stream.binder.rocketmq.autoconfigurate.RocketMQBinderAutoConfiguration;
import com.alibaba.cloud.stream.binder.rocketmq.constant.RocketMQConst;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageProducer;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(classes = RocketMQMessageChannelBinderTest.TestConfig.class,
		webEnvironment = NONE,
		properties = { "spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876",
				"spring.cloud.stream.bindings.output.destination=TopicOrderTest",
				"spring.cloud.stream.bindings.output.content-type=application/json",

				"spring.cloud.stream.bindings.input1.destination=TopicOrderTest",
				"spring.cloud.stream.bindings.input1.content-type=application/json",
				"spring.cloud.stream.bindings.input1.group=test-group1",
				"spring.cloud.stream.rocketmq.bindings.input1.consumer.push.orderly=true",
				"spring.cloud.stream.bindings.input1.consumer.maxAttempts=1",
				"spring.cloud.stream.bindings.input2.destination=TopicOrderTest",
				"spring.cloud.stream.bindings.input2.content-type=application/json",
				"spring.cloud.stream.bindings.input2.group=test-group2",
				"spring.cloud.stream.rocketmq.bindings.input2.consumer.push.orderly=false",
				"spring.cloud.stream.rocketmq.bindings.input2.consumer.subscription=tag1" })
public class RocketMQMessageChannelBinderTest {

	@Resource
	RocketMQMessageChannelBinder binder;

	@Test
	public void createConsumerEndpoint() throws Exception {
		TestConsumerDestination destination = new TestConsumerDestination("test");
		MessageProducer consumerEndpoint = binder.createConsumerEndpoint(destination,
				"test",
				new ExtendedConsumerProperties<>(new RocketMQConsumerProperties()));
		Assertions.assertThat(consumerEndpoint).isNotNull();
	}

	@Test
	public void createAnymousConsumerEndpoint() throws Exception {
		ExtendedConsumerProperties<RocketMQConsumerProperties> extendedConsumerProperties = new ExtendedConsumerProperties<>(
				new RocketMQConsumerProperties());

		TestConsumerDestination destination = new TestConsumerDestination("test");
		MessageProducer consumerEndpoint = binder.createConsumerEndpoint(destination,
				null, extendedConsumerProperties);
		Assertions.assertThat(consumerEndpoint).isNotNull();
		Assertions.assertThat(extendedConsumerProperties.getExtension().getGroup())
				.isEqualTo(RocketMQConst.DEFAULT_GROUP + "_test");
	}

	@Test
	public void createDLQAnymousConsumerEndpoint() throws Exception {
		TestConsumerDestination destination = new TestConsumerDestination("%DLQ%test");
		Assertions.assertThatThrownBy(() -> {
			MessageProducer consumerEndpoint = binder.createConsumerEndpoint(destination,
					null,
					new ExtendedConsumerProperties<>(new RocketMQConsumerProperties()));
		});
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ ExtendedBindingHandlerMappingsProviderConfiguration.class,
			RocketMQBinderAutoConfiguration.class })
	public static class TestConfig {

	}

}
