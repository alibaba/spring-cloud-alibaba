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

import com.alibaba.cloud.stream.binder.rocketmq.constant.RocketMQConst;
import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQMessageConverterSupport;
import org.apache.rocketmq.common.message.MessageConst;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * @author Sorie
 */
@ExtendWith(OutputCaptureExtension.class)
public class RocketMQMessageConverterSupportTest {

	@Test
	public void convertMessage2MQBlankHeaderTest() {
		String destination = "test";
		Message message = MessageBuilder.withPayload("msg")
				.setHeader(MessageConst.PROPERTY_TAGS, "a")
				.setHeader("test", "")
				.build();
		org.apache.rocketmq.common.message.Message rkmqMsg =
				RocketMQMessageConverterSupport.convertMessage2MQ(destination, message);
		String testProp = rkmqMsg.getProperty("test");
		String tagProp = rkmqMsg.getProperty(MessageConst.PROPERTY_TAGS);
		assertThat(testProp).isNull();
		assertThat(tagProp).isEqualTo("a");
	}

	@Test
	public void nonNumericDelayTimeLevelHeaderFallsBackToZero() {
		Message<String> message = MessageBuilder.withPayload("msg")
				.setHeader(RocketMQConst.PROPERTY_DELAY_TIME_LEVEL, "not-a-number")
				.build();

		org.apache.rocketmq.common.message.Message rkmqMsg =
				RocketMQMessageConverterSupport.convertMessage2MQ("topic", message);

		assertThat(rkmqMsg.getDelayTimeLevel()).isEqualTo(0);
		assertThat(rkmqMsg.getFlag()).isEqualTo(0);
	}

	@Test
	public void nonNumericFlagHeaderFallsBackToZero() {
		Message<String> message = MessageBuilder.withPayload("msg")
				.setHeader(RocketMQConst.Headers.FLAG, "not-a-number")
				.build();

		org.apache.rocketmq.common.message.Message rkmqMsg =
				RocketMQMessageConverterSupport.convertMessage2MQ("topic", message);

		assertThat(rkmqMsg.getFlag()).isEqualTo(0);
	}

	@Test
	public void invalidNumericHeaderDoesNotPropagateException() {
		Message<String> message = MessageBuilder.withPayload("msg")
				.setHeader(RocketMQConst.PROPERTY_DELAY_TIME_LEVEL, "x")
				.setHeader(RocketMQConst.Headers.FLAG, "y")
				.build();

		assertThatCode(() ->
				RocketMQMessageConverterSupport.convertMessage2MQ("topic", message))
				.doesNotThrowAnyException();
	}

	@Test
	public void nonNumericFlagHeaderLogsWarning(CapturedOutput output) {
		Message<String> message = MessageBuilder.withPayload("msg")
				.setHeader(RocketMQConst.Headers.FLAG, "flag-not-a-number")
				.build();

		RocketMQMessageConverterSupport.convertMessage2MQ("topic", message);

		// The malformed header must no longer be swallowed silently: the warning
		// names the offending header and echoes its raw value.
		assertThat(output).contains(RocketMQConst.Headers.FLAG)
				.contains("flag-not-a-number");
	}

	@Test
	public void validDelayLevelWithNonNumericFlagIsPreservedAndWarnsOnlyFlag(
			CapturedOutput output) {
		Message<String> message = MessageBuilder.withPayload("msg")
				.setHeader(RocketMQConst.PROPERTY_DELAY_TIME_LEVEL, "3")
				.setHeader(RocketMQConst.Headers.FLAG, "flag-bad")
				.build();

		org.apache.rocketmq.common.message.Message rkmqMsg =
				RocketMQMessageConverterSupport.convertMessage2MQ("topic", message);

		// A valid delay level must survive a malformed flag on the same message;
		// the two headers are parsed independently.
		assertThat(rkmqMsg.getDelayTimeLevel()).isEqualTo(3);
		assertThat(rkmqMsg.getFlag()).isEqualTo(0);
		// The warning names the flag header only; the valid delay level must not
		// be reported as having fallen back.
		assertThat(output).contains("flag-bad")
				.doesNotContain("'" + RocketMQConst.PROPERTY_DELAY_TIME_LEVEL + "'");
	}

	@Test
	public void validFlagWithNonNumericDelayLevelIsPreservedAndWarnsOnlyDelayLevel(
			CapturedOutput output) {
		Message<String> message = MessageBuilder.withPayload("msg")
				.setHeader(RocketMQConst.PROPERTY_DELAY_TIME_LEVEL, "delay-bad")
				.setHeader(RocketMQConst.Headers.FLAG, "7")
				.build();

		org.apache.rocketmq.common.message.Message rkmqMsg =
				RocketMQMessageConverterSupport.convertMessage2MQ("topic", message);

		// A valid flag must survive a malformed delay level on the same message;
		// the two headers are parsed independently.
		assertThat(rkmqMsg.getFlag()).isEqualTo(7);
		assertThat(rkmqMsg.getDelayTimeLevel()).isEqualTo(0);
		// The warning names the delay-level header only; the valid flag must not
		// be reported as having fallen back.
		assertThat(output).contains("delay-bad")
				.doesNotContain("'" + RocketMQConst.Headers.FLAG + "'");
	}
}
