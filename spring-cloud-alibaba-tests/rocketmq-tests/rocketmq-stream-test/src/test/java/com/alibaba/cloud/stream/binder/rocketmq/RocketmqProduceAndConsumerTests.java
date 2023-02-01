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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.alibaba.cloud.stream.binder.rocketmq.fixture.RocketmqBinderProcessor;
import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.common.message.MessageConst;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

@SpringCloudAlibaba(composeFiles = "docker/rocket-compose-test.yml", serviceName = "rocketmq-standalone")
@TestExtend(time = 6 * TIME_OUT)
@DirtiesContext
@ImportAutoConfiguration(value = {}, exclude = { DataSourceAutoConfiguration.class,
		TransactionAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class })
@SpringBootTest(classes = RocketmqBinderProcessor.class, webEnvironment = NONE, properties = {
		"spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876,127.0.0.1:9877",
		"spring.cloud.stream.rocketmq.binder.group=flaky-group",
		// "spring.cloud.stream.rocketmq.binder.consumer-group=flaky-group",
		// "spring.cloud.stream.pollable-source=pollable",
		"spring.cloud.stream.bindings.uppercaseFunction-out-0.destination=TopicOrderTest",
		"spring.cloud.stream.bindings.uppercaseFunction-out-0.content-type=application/json",
		"spring.cloud.stream.bindings.uppercaseFunction-out-0.group=test-group1",
		"spring.cloud.stream.bindings.uppercaseFunction-in-0.destination=TopicOrderTest",
		"spring.cloud.stream.bindings.uppercaseFunction-in-0.content-type=application/json",
		"spring.cloud.stream.bindings.uppercaseFunction-in-0.group=test-group1",
		"spring.cloud.stream.bindings.uppercaseFunction-in-0.consumer.push.orderly=true",
		"spring.cloud.stream.bindings.uppercaseFunction-in-0.consumer.maxAttempts=1" })
public class RocketmqProduceAndConsumerTests {

	@Autowired
	private MessageCollector collector;

	@Autowired
	@Qualifier("uppercaseFunction-in-0")
	private MessageChannel input1;

	@Autowired
	@Qualifier("uppercaseFunction-out-0")
	private MessageChannel output;

	@BeforeAll
	public static void prepare() {

	}

	@BeforeEach
	public void setup() {
		String key = "KEY";
		String messageId = "1";
		Map<String, Object> headers = new HashMap<>();
		headers.put(MessageConst.PROPERTY_KEYS, key);
		headers.put(MessageConst.PROPERTY_TAGS, "TagA");
		headers.put(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, messageId);
		Message<String> msg = new GenericMessage(JSON.toJSONString("Hello RocketMQ"),
				headers);
		input1.send(msg);
	}

	@Test
	public void testConsumeAndProduce() throws Exception {
		BlockingQueue<Message<?>> messages = this.collector.forChannel(this.output);

		MatcherAssert.assertThat(messages, receivesPayloadThat(is("\"HELLO ROCKETMQ\"")));
	}

}
