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

package com.alibaba.cloud.governance.auth.webflux;

import java.nio.charset.StandardCharsets;

import com.alibaba.cloud.commons.pair.Pair;
import com.alibaba.cloud.governance.auth.AuthValidator;
import com.alibaba.cloud.governance.auth.util.IpUtil;
import org.jose4j.jwt.JwtClaims;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

public class AuthWebFluxFilter implements WebFilter {

	private AuthValidator authValidator;

	public AuthWebFluxFilter(AuthValidator authValidator) {
		this.authValidator = authValidator;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String sourceIp = null;
		String destIp = null;
		String remoteIp = null;
		if (request.getRemoteAddress() != null) {
			sourceIp = request.getRemoteAddress().getAddress().getHostAddress();
		}
		if (request.getLocalAddress() != null) {
			destIp = request.getLocalAddress().getAddress().getHostAddress();
		}
		remoteIp = IpUtil.getRemoteIpAddress(request);
		if (!authValidator.validateIp(sourceIp, destIp, remoteIp)) {
			return ret401(exchange);
		}
		String host = request.getHeaders().getFirst(HttpHeaders.HOST);
		int port = request.getLocalAddress().getPort();
		String method = request.getMethodValue();
		String path = request.getPath().value();
		if (!authValidator.validateTargetRule(host, port, method, path)) {
			return ret401(exchange);
		}
		if (!authValidator.validateHeader(request.getHeaders())) {
			return ret401(exchange);
		}
		JwtClaims jwtClaims = null;
		if (!authValidator.isEmptyJwtRule()) {
			Pair<JwtClaims, Boolean> jwtClaimsBooleanPair = authValidator
					.validateJwt(request.getQueryParams(), request.getHeaders());
			if (!jwtClaimsBooleanPair.getRight()) {
				return ret401(exchange);
			}
			jwtClaims = jwtClaimsBooleanPair.getLeft();
		}

		if (jwtClaims == null && authValidator.isEmptyJwtAuthRule()) {
			return chain.filter(exchange);
		}

		if (!authValidator.validateJwtAuthRule(jwtClaims)) {
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