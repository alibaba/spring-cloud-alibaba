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

package com.alibaba.cloud.examples;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.StringObjectMapBuilder;

/**
 * @author freeman
 */
@SpringBootApplication
public class RocketMQComprehensiveApplication {

	private static final Logger log = LoggerFactory
			.getLogger(RocketMQComprehensiveApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RocketMQComprehensiveApplication.class, args);
	}

	@Bean
	public Supplier<Flux<User>> producer() {
		return () -> Flux.interval(Duration.ofSeconds(2)).map(id -> {
			User user = new User();
			user.setId(id.toString());
			user.setName("freeman");
			user.setMeta(new StringObjectMapBuilder()
					.put("hobbies", Arrays.asList("movies", "songs")).put("age", 21)
					.get());
			return user;
		}).log();
	}

	@Bean
	public Function<Flux<User>, Flux<User>> processor() {
		return flux -> flux.map(user -> {
			user.setId(String.valueOf(
					Long.parseLong(user.getId()) * Long.parseLong(user.getId())));
			return user;
		});
	}

	@Bean
	public Consumer<User> consumer() {
		return num -> log.info(num.toString());
	}

}
