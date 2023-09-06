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

package com.alibaba.cloud.examples.broadcast;

import java.util.function.Consumer;

import com.alibaba.cloud.examples.common.SimpleMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

/**
 * @author sorie
 */
@SpringBootApplication
public class RocketMQBroadcastConsumer1Application {

	private static final Logger log = LoggerFactory
			.getLogger(RocketMQBroadcastConsumer1Application.class);

	public static void main(String[] args) {
		SpringApplication.run(RocketMQBroadcastConsumer1Application.class, args);
	}

	@Bean
	public Consumer<Message<SimpleMsg>> consumer() {
		return msg -> {
			log.info(Thread.currentThread().getName()
					+ " Consumer1 Receive New Messages: " + msg.getPayload().getMsg());
		};
	}

}
