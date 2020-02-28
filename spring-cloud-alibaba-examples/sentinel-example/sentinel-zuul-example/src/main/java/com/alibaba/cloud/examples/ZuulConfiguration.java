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

import javax.servlet.http.HttpServletRequest;

import com.alibaba.csp.sentinel.adapter.gateway.zuul.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.BlockResponse;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.ZuulBlockFallbackProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration
public class ZuulConfiguration {

	@Bean
	public ZuulBlockFallbackProvider zuulBlockFallbackProvider1() {
		return new ZuulBlockFallbackProvider() {
			@Override
			public String getRoute() {
				return "*";
			}

			@Override
			public BlockResponse fallbackResponse(String route, Throwable cause) {
				if (route.equals("my-service3")) {
					return new BlockResponse(433, "Sentinel Block3", route);
				}
				else if (route.equals("my-service4")) {
					return new BlockResponse(444, "my-service4", route);
				}
				else {
					return new BlockResponse(499, "Sentinel Block 499", route);
				}
			}
		};
	}

	@Bean
	public RequestOriginParser requestOriginParser() {
		return new RequestOriginParser() {

			@Override
			public String parseOrigin(HttpServletRequest request) {
				return "123";
			}
		};
	}

}
