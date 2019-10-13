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

package com.alibaba.cloud.examples.service;

import com.alibaba.cloud.examples.fallback.EchoServiceFallbackFactory;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lengleng
 * @date 2019-08-01
 * <p>
 * example feign client
 */
@FeignClient(name = "service-provider",
		fallbackFactory = EchoServiceFallbackFactory.class)
public interface EchoService {

	/**
	 * 调用服务提供方的输出接口
	 * @param str 用户输入
	 * @return
	 */
	@GetMapping("/echo/{str}")
	String echo(@PathVariable("str") String str);

}
