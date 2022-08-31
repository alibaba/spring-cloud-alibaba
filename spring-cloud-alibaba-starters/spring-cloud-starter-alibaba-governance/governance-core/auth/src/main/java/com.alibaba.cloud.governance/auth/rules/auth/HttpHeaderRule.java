package com.alibaba.cloud.governance.auth.rules.auth;

import com.alibaba.cloud.governance.auth.rules.AndRule;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;

import java.util.Map;

public class HttpHeaderRule {

	private String name;

	private Map<String, AndRule<HeaderMatcher>> headers;

	public HttpHeaderRule(String name, Map<String, AndRule<HeaderMatcher>> headers) {
		this.name = name;
		this.headers = headers;
	}

	public String getName() {
		return name;
	}

	public Map<String, AndRule<HeaderMatcher>> getHeaders() {
		return headers;
	}

}
