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

import com.alibaba.cloud.examples.RocketMQConsumerApplication.MySink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@SpringBootApplication
@EnableBinding({ MySink.class })
public class RocketMQConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RocketMQConsumerApplication.class, args);
	}

	@Bean
	public ConsumerCustomRunner customRunner() {
		return new ConsumerCustomRunner();
	}

	public interface MySink {

		@Input("input1")
		SubscribableChannel input1();

		@Input("input2")
		SubscribableChannel input2();

		@Input("input3")
		SubscribableChannel input3();

		@Input("input4")
		SubscribableChannel input4();

		@Input("input5")
		PollableMessageSource input5();

	}

	public static class ConsumerCustomRunner implements CommandLineRunner {

		@Autowired
		private MySink mySink;

		@Override
		public void run(String... args) throws InterruptedException {
			while (true) {
				mySink.input5().poll(m -> {
					String payload = (String) m.getPayload();
					System.out.println("pull msg: " + payload);
				}, new ParameterizedTypeReference<String>() {
				});
				Thread.sleep(2_000);
			}
		}

	}

}
