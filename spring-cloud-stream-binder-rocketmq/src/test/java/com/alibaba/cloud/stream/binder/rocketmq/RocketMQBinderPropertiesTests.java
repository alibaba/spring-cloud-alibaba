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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.cloud.stream.binder.rocketmq.config.RocketMQBinderAutoConfiguration;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;

/**
 * RocketMQ binder configuration properties unit test class
 *
 * @author <a href="mailto:jiashuai.xie01@gmail.com">Xiejiashuai</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { RocketMQBinderAutoConfiguration.class,
		RocketMQBinderPropertiesTests.class })
@TestPropertySource(properties = {
		"spring.cloud.stream.rocketmq.binder.nameServer=127.0.0.1:6666",
		"spring.cloud.stream.rocketmq.bindings.output.producer.maxMessageSize=12345",
		"spring.cloud.stream.rocketmq.bindings.output.producer.sendMessageTimeout=100",
		"spring.cloud.stream.rocketmq.bindings.output.producer.compressMessageBodyThreshold=1111",
		"spring.cloud.stream.rocketmq.bindings.input.consumer.tags=test",
		"spring.cloud.stream.rocketmq.bindings.input.consumer.delayLevelWhenNextConsume=100",
		"spring.cloud.stream.rocketmq.bindings.input.consumer.enabled=false", })
public class RocketMQBinderPropertiesTests {

	@Autowired
	private RocketMQMessageChannelBinder rocketMQMessageChannelBinder;

	@Autowired
	private RocketMQBinderConfigurationProperties configurationProperties;

	@Test
	public void testRocketMQBinderConfigurationProperties() throws Exception {
		assertNotNull(this.rocketMQMessageChannelBinder);
		assertEquals("127.0.0.1:6666", configurationProperties.getNameServer());
	}

	@Test
	public void testRocketMQProducerProperties() {
		RocketMQProducerProperties rocketMQProducerProperties = rocketMQMessageChannelBinder
				.getExtendedProducerProperties("output");
		assertEquals(100, rocketMQProducerProperties.getSendMessageTimeout());
		assertEquals(Integer.valueOf(12345),
				rocketMQProducerProperties.getMaxMessageSize());
		assertEquals(1111, rocketMQProducerProperties.getCompressMessageBodyThreshold());
	}

	@Test
	public void testRocketMQConsumerProperties() {
		RocketMQConsumerProperties rocketMQConsumerProperties = rocketMQMessageChannelBinder
				.getExtendedConsumerProperties("input");
		assertEquals("test", rocketMQConsumerProperties.getTags());
		assertEquals(100, rocketMQConsumerProperties.getDelayLevelWhenNextConsume());
		assertEquals(Boolean.valueOf("false"), rocketMQConsumerProperties.getEnabled());
	}

}
