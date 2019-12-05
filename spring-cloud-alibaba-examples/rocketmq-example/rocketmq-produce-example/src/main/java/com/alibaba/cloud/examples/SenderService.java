/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.examples;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.cloud.examples.RocketMQProduceApplication.MySource;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.support.RocketMQHeaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Service
public class SenderService {

	@Autowired
	private MySource source;

	public void send(String msg) throws Exception {
		source.output1().send(MessageBuilder.withPayload(msg).build());
	}

	public <T> void sendWithTags(T msg, String tag) throws Exception {
		Message message = MessageBuilder.createMessage(msg,
				new MessageHeaders(Stream.of(tag).collect(Collectors
						.toMap(str -> MessageConst.PROPERTY_TAGS, String::toString))));
		source.output1().send(message);
	}

	public <T> void sendObject(T msg, String tag) throws Exception {
		Message message = MessageBuilder.withPayload(msg)
				.setHeader(MessageConst.PROPERTY_TAGS, tag)
				.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
				.build();
		source.output1().send(message);
	}

	public <T> void sendTransactionalMsg(T msg, int num) throws Exception {
		MessageBuilder builder = MessageBuilder.withPayload(msg)
				.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
		builder.setHeader("test", String.valueOf(num));
		builder.setHeader(RocketMQHeaders.TAGS, "binder");
		Message message = builder.build();
		source.output2().send(message);
	}

}
