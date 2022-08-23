package com.alibaba.cloud.governance.auth.webflux;

import com.alibaba.cloud.governance.common.rules.manager.*;
import com.alibaba.cloud.governance.common.rules.util.IpUtil;
import org.jose4j.jwt.JwtClaims;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class AuthWebFluxFilter implements WebFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String sourceIp = null, destIp = null, remoteIp = null;
		if (request.getRemoteAddress() != null) {
			sourceIp = request.getRemoteAddress().getAddress().getHostAddress();
		}
		if (request.getLocalAddress() != null) {
			destIp = request.getRemoteAddress().getAddress().getHostAddress();
		}
		remoteIp = IpUtil.getRemoteIpAddress(request);
		if (!IpBlockRuleManager.isValid(sourceIp, destIp, remoteIp)) {
			return ret401(exchange);
		}
		String host = request.getRemoteAddress().getHostName();
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
		if ((jwtClaims = JwtRuleManager.isValid(request.getQueryParams(),
				request.getHeaders())) == null) {
			return ret401(exchange);
		}
		if (!JwtAuthRuleManager.isValid(jwtClaims)) {
			return ret401(exchange);
		}
		return chain.filter(exchange);
	}

	private Mono<Void> ret401(ServerWebExchange exchange) {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		return exchange.getResponse().setComplete();
	}

}
