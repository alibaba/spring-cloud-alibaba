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

package com.alibaba.cloud.examples.configuration;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Provide the external exposure interface of the service calling client.
 *
 * @author raozihao
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
@FeignClient(name = "openfeign-example", url = "https://httpbin.org", contextId = "openfeign-example", fallbackFactory = EchoServiceFallbackFactory.class)
public interface HttpbinClient {

	@GetMapping("/delay/3")
	String delay();

	@GetMapping("/status/500")
	String status500();


	@GetMapping("/get")
	String get();

}
