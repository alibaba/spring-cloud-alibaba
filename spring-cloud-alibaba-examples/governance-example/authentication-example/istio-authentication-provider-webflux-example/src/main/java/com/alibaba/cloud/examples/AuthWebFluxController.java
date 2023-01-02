/*
 * Copyright 2013-2022 the original author or authors.
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

import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
public class AuthWebFluxController {

	@RequestMapping("/auth")
	public Mono<String> auth(ServerWebExchange request) {
		String resp = "received request from "
				+ request.getRequest().getRemoteAddress().getAddress().getHostAddress()
				+ ", local addr is "
				+ request.getRequest().getLocalAddress().getAddress().getHostAddress()
				+ ", local host is "
				+ request.getRequest().getLocalAddress().getAddress().getHostName()
				+ ", request path is" + request.getRequest().getURI().getPath();
		return Mono.just(resp);
	}

}
