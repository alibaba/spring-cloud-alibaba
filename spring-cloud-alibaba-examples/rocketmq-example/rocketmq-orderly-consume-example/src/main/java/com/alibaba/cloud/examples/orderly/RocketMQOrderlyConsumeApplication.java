/*
 * Copyright 2013-2022 the original author or authors.
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

package com.alibaba.cloud.examples.orderly;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQMessageConverterSupport;
import org.apache.rocketmq.common.message.MessageConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
/**
 * @author sorie
 */
@SpringBootApplication
public class RocketMQOrderlyConsumeApplication {
	private static final Logger log = LoggerFactory
			.getLogger(RocketMQOrderlyConsumeApplication.class);

	/***
	 * tag array.
	 */
	public static final String[] tags = new String[] {"TagA", "TagB", "TagC", "TagD", "TagE"};

	public static void main(String[] args) {
		SpringApplication.run(RocketMQOrderlyConsumeApplication.class, args);
	}

	@Bean
	public Supplier<Flux<Message<String>>> producer() {
		return () -> {
			return Flux.range(0, 100).map(i -> {
				String key = "KEY" + i;
				Map<String, Object> headers = new HashMap<>();
				headers.put(MessageConst.PROPERTY_KEYS, key);
				headers.put(MessageConst.PROPERTY_TAGS, tags[i % tags.length]);
				headers.put(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, i);
				Message<String> msg = new GenericMessage("Hello RocketMQ " + i, headers);
				return msg;
			}).log();
		};
	}

	@Bean
	public Consumer<Message<String>> consumer() {
		return msg -> {
			String tagHeaderKey = RocketMQMessageConverterSupport.toRocketHeaderKey(
					MessageConst.PROPERTY_TAGS).toString();
			log.info(Thread.currentThread().getName() + " Receive New Messages: " + msg.getPayload() + " TAG:" +
					msg.getHeaders().get(tagHeaderKey).toString());
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException ignored) {
			}
		};
	}

}
