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

package com.alibaba.cloud.examples.pollable;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.cloud.examples.common.SimpleMsg;
import org.apache.rocketmq.common.message.MessageConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

/**
 * @author sorie
 */
@SpringBootApplication
public class RocketMQPollableConsumeApplication {

	private static final Logger log = LoggerFactory
			.getLogger(RocketMQPollableConsumeApplication.class);

	@Autowired
	private StreamBridge streamBridge;

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(RocketMQPollableConsumeApplication.class, args);
		PollableMessageSource destIn = context.getBean(PollableMessageSource.class);
		new Thread(() -> {
			while (true) {
				try {
					if (!destIn.poll((m) -> {
						SimpleMsg newPayload = (SimpleMsg) m.getPayload();
						System.out.println(newPayload.getMsg());
					}, new ParameterizedTypeReference<SimpleMsg>() {
					})) {
						Thread.sleep(1000);
					}
				}
				catch (Exception e) {
					// handle failure
				}
			}
		}).start();
	}

	@Bean
	public ApplicationRunner producer() {
		return args -> {
			for (int i = 0; i < 100; i++) {
				String key = "KEY" + i;
				Map<String, Object> headers = new HashMap<>();
				headers.put(MessageConst.PROPERTY_KEYS, key);
				Message<SimpleMsg> msg = new GenericMessage(
						new SimpleMsg("Hello RocketMQ " + i), headers);
				streamBridge.send("producer-out-0", msg);
			}
		};
	}

}
