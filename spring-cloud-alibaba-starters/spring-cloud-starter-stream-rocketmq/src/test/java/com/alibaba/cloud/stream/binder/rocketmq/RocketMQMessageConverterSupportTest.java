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

import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQMessageConverterSupport;
import org.apache.rocketmq.common.message.MessageConst;
import org.junit.jupiter.api.Test;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sorie
 */
public class RocketMQMessageConverterSupportTest {

	@Test
	public void convertMessage2MQBlankHeaderTest() {
		String destination = "test";
		Message message = MessageBuilder.withPayload("msg")
				.setHeader(MessageConst.PROPERTY_TAGS, "a").setHeader("test", "").build();
		org.apache.rocketmq.common.message.Message rkmqMsg = RocketMQMessageConverterSupport
				.convertMessage2MQ(destination, message);
		String testProp = rkmqMsg.getProperty("test");
		String tagProp = rkmqMsg.getProperty(MessageConst.PROPERTY_TAGS);
		assertThat(testProp).isNull();
		assertThat(tagProp).isEqualTo("a");
	}

}
