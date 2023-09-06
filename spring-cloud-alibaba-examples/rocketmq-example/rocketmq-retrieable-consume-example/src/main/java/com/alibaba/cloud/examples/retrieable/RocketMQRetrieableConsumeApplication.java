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

package com.alibaba.cloud.examples.retrieable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.alibaba.cloud.examples.common.SimpleMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

/**
 * RocketMQ Retrieable Consume Example .
 *
 * @author Palmer.Xu
 */
@SpringBootApplication
public class RocketMQRetrieableConsumeApplication {

	private static final Logger log = LoggerFactory
			.getLogger(RocketMQRetrieableConsumeApplication.class);

	@Autowired
	private StreamBridge streamBridge;

	public static void main(String[] args) {
		SpringApplication.run(RocketMQRetrieableConsumeApplication.class, args);
	}

	@Bean
	public ApplicationRunner producer() {
		return args -> {
			Map<String, Object> headers = new HashMap<>();
			Message<SimpleMsg> msg = new GenericMessage(
					new SimpleMsg("Hello RocketMQ For Retrieable ."), headers);
			streamBridge.send("producer-out-0", msg);
		};
	}

	@Bean
	public Consumer<Message<SimpleMsg>> consumer() {
		return msg -> {
			throw new RuntimeException("mock exception.");
		};
	}

}
