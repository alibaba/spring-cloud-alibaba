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

package com.alibaba.cloud.examples.tx;

import java.util.function.Consumer;

import com.alibaba.cloud.examples.common.SimpleMsg;
import com.alibaba.cloud.stream.binder.rocketmq.constant.RocketMQConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

/**
 * @author sorie
 */
@SpringBootApplication
public class RocketMQTxApplication {

	private static final Logger log = LoggerFactory
			.getLogger(RocketMQTxApplication.class);

	@Autowired
	private StreamBridge streamBridge;

	public static void main(String[] args) {
		SpringApplication.run(RocketMQTxApplication.class, args);
	}

	@Bean
	public ApplicationRunner producer() {
		return args -> {
			for (int i = 1; i <= 4; i++) {
				MessageBuilder builder = MessageBuilder
						.withPayload(new SimpleMsg("Hello Tx msg " + i));
				builder.setHeader("test", String.valueOf(i)).setHeader(
						MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
				builder.setHeader(RocketMQConst.USER_TRANSACTIONAL_ARGS, "binder");
				Message<SimpleMsg> msg = builder.build();
				streamBridge.send("producer-out-0", msg);
				System.out.println("send Msg:" + msg.toString());
			}
		};
	}

	@Bean
	public Consumer<Message<SimpleMsg>> consumer() {
		return msg -> {
			Object arg = msg.getHeaders();
			log.info(Thread.currentThread().getName() + " Receive New Messages: "
					+ msg.getPayload().getMsg() + " ARG:" + arg.toString());
		};
	}

}
