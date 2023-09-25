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

package com.alibaba.example.zuul.controller;

import javax.annotation.Resource;

import com.alibaba.example.zuul.service.AddZuulRoutingRuleService;
import com.alibaba.example.zuul.service.UpdateZuulRoutingRuleService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo-yx
 * @author <a href="1481556636@qq.com"></a>
 */

@RestController
public class ZuulConsumerController {

	@Resource
	private AddZuulRoutingRuleService addZuulRoutingRuleService;

	@Resource
	private UpdateZuulRoutingRuleService updateZuulRoutingRuleService;

	@GetMapping("/addRule")
	public void getDataFromControlPlaneTest() {

		addZuulRoutingRuleService.getDataFromControlPlaneTest();
	}

	@GetMapping("/updateRule")
	public void updateDataFromControlPlaneTest() {

		updateZuulRoutingRuleService.updateDataFromControlPlaneTest();
	}

}
