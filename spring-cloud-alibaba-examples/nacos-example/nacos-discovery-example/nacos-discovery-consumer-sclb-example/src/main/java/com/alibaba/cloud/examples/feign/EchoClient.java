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

package com.alibaba.cloud.examples.feign;

import com.alibaba.cloud.examples.config.FeignConfiguration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Provide the external exposure interface of the service calling client.
 *
 * @author fangjian0423, MieAh
 */
@FeignClient(name = "service-provider", fallback = EchoClientFallback.class, configuration = FeignConfiguration.class)
public interface EchoClient {

	/**
	 * Call the echo method of the remote provider or roll back when the service is blown.
	 *
	 * @param str str
	 * @return {@link String}
	 */
	@GetMapping("/echo/{str}")
	String echo(@PathVariable("str") String str);

	/**
	 * Call the divide method of the remote provider or roll back when the service is blown.
	 *
	 * @param a a
	 * @param b b
	 * @return {@link String}
	 */
	@GetMapping("/divide")
	String divide(@RequestParam("a") Integer a, @RequestParam("b") Integer b);

	/**
	 * Test that the default method calls the remote method is still a remote call.
	 *
	 * @param a a
	 * @return {@link String}
	 */
	default String divide(Integer a) {
		return divide(a, 0);
	}

	/**
	 * Call the notFound method of the remote provider or roll back when the service is blown.
	 *
	 * @return {@link String}
	 */
	@GetMapping("/notFound")
	String notFound();
}
