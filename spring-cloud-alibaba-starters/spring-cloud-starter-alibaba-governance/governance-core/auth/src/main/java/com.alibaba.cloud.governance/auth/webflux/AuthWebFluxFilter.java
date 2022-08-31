package com.alibaba.cloud.governance.auth.webflux;

import com.alibaba.cloud.governance.auth.rules.manager.*;
import com.alibaba.cloud.governance.auth.rules.util.IpUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jwt.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class AuthWebFluxFilter implements WebFilter {

	private final static Logger log = LoggerFactory.getLogger(AuthWebFluxFilter.class);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String sourceIp = null, destIp = null, remoteIp = null;
		if (request.getRemoteAddress() != null) {
			sourceIp = request.getRemoteAddress().getAddress().getHostAddress();
		}
		if (request.getLocalAddress() != null) {
			destIp = request.getLocalAddress().getAddress().getHostAddress();
		}
		remoteIp = IpUtil.getRemoteIpAddress(request);
		if (!IpBlockRuleManager.isValid(sourceIp, destIp, remoteIp)) {
			return ret401(exchange);
		}
		String host = request.getHeaders().getFirst(HttpHeaders.HOST);
		int port = request.getLocalAddress().getPort();
		String method = request.getMethodValue();
		String path = request.getPath().value();
		if (!TargetRuleManager.isValid(host, port, method, path)) {
			return ret401(exchange);
		}
		if (!HeaderRuleManager.isValid(request.getHeaders())) {
			return ret401(exchange);
		}
		JwtClaims jwtClaims = null;
		if (!JwtRuleManager.isEmpty()) {
			Pair<JwtClaims, Boolean> jwtClaimsBooleanPair = JwtRuleManager
					.isValid(request.getQueryParams(), request.getHeaders());
			if (!jwtClaimsBooleanPair.getRight()) {
				return ret401(exchange);
			}
			jwtClaims = jwtClaimsBooleanPair.getLeft();
		}

		if (jwtClaims == null && JwtAuthRuleManager.isEmpty()) {
			return chain.filter(exchange);
		}

		if (!JwtAuthRuleManager.isValid(jwtClaims)) {
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
