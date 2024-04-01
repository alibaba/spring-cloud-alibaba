/*
 * Copyright 2023-2024 the original author or authors.
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

package com.alibaba.cloud.ai.example.tongyi.controller;

import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.service.TongYiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo
 * @since 2023.0.0.0
 */

@RestController
@RequestMapping("/ai")
@CrossOrigin
public class TongYiController {

	@Autowired
	private TongYiService tongyiService;

	@GetMapping("/example")
	public String completion(
			@RequestParam(value = "message", defaultValue = "Tell me a joke")
			String message
	) {

		return tongyiService.completion(message);
	}

	@GetMapping("/stream")
	public Map<String, String> streamCompletion(
			@RequestParam(value = "message", defaultValue = "请告诉我西红柿炖牛腩怎么做？")
			String message
	) {

		return tongyiService.streamCompletion(message);
	}

}
