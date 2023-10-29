/*
 * Copyright 2022-2023 the original author or authors.
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.examples.feignclient.MvcClient;
import com.alibaba.cloud.examples.feignclient.WebfluxClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenfeignTestController {

	@Resource
	private MvcClient mvcClient;

	@Resource
	private WebfluxClient webfluxClient;

	@GetMapping("/openfeign/getMvc")
	public String getB(HttpServletRequest httpServletRequest) {
		return mvcClient.getMvc();
	}

	@GetMapping("/openfeign/getWebflux")
	public String getC(HttpServletRequest httpServletRequest) {
		return webfluxClient.getWebflux();
	}

}
