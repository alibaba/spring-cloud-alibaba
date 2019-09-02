/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * @author xiaolongzuo
 */
@RestController
public class EchoController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EchoController.class);

	@GetMapping(value = "/echo/{str}", produces = "application/json")
	public String echo(@PathVariable String str) {
		LOGGER.info("-----------收到消费者请求-----------");
		LOGGER.info("收到消费者传递的参数：" + str);
		String result = "Nice to meet you, too.";
		LOGGER.info("提供者返回结果：" + result);
		return result;
	}
}
