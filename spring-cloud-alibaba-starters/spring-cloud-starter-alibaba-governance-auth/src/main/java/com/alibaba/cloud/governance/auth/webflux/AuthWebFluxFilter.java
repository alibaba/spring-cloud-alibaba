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

package com.alibaba.cloud.governance.auth.webflux;

import java.nio.charset.StandardCharsets;

import com.alibaba.cloud.governance.auth.util.IpUtil;
import com.alibaba.cloud.governance.auth.validator.AuthValidator;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class AuthWebFluxFilter implements WebFilter {

	private final AuthValidator authValidator;

	public AuthWebFluxFilter(AuthValidator authValidator) {
		this.authValidator = authValidator;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String destIp = null;
		String remoteIp = null;
		String sourceIp = null;
		if (request.getRemoteAddress() != null) {
			sourceIp = request.getRemoteAddress().getAddress().getHostAddress();
		}
		if (request.getLocalAddress() != null) {
			destIp = request.getLocalAddress().getAddress().getHostAddress();
		}
		remoteIp = IpUtil.getRemoteIpAddress(request);
		String host = request.getHeaders().getFirst(HttpHeaders.HOST);
		int port = request.getLocalAddress().getPort();
		String method = request.getMethodValue();
		String path = request.getPath().value();
		HttpHeaders headers = request.getHeaders();
		MultiValueMap<String, String> params = request.getQueryParams();
		AuthValidator.UnifiedHttpRequest.UnifiedHttpRequestBuilder builder = new AuthValidator.UnifiedHttpRequest.UnifiedHttpRequestBuilder();
		AuthValidator.UnifiedHttpRequest unifiedHttpRequest = builder.setDestIp(destIp)
				.setRemoteIp(remoteIp).setSourceIp(sourceIp).setHost(host).setPort(port)
				.setMethod(method).setPath(path).setHeaders(headers).setParams(params)
				.build();

		if (!authValidator.validate(unifiedHttpRequest)) {
			return ret401(exchange);
		}
		return chain.filter(exchange);
	}

	private Mono<Void> ret401(ServerWebExchange exchange) {
		return ret401(exchange, "Auth failed, please check the request and auth rule");
	}

	private Mono<Void> ret401(ServerWebExchange exchange, String errorMsg) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		byte[] data = errorMsg.getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = response.bufferFactory().wrap(data);
		return response.writeWith(Mono.just(buffer));
	}

}
