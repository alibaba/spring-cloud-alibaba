package com.alibaba.cloud.governance.common.rules.auth;

import com.alibaba.cloud.governance.common.rules.AndRule;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;

public class IdentityRule {

	private String name;

	// in k8s, it is service account
	private AndRule<StringMatcher> identities;

	public IdentityRule(String name, AndRule<StringMatcher> identities) {
		this.name = name;
		this.identities = identities;
	}

	public AndRule<StringMatcher> getIdentities() {
		return identities;
	}

	public String getName() {
		return name;
	}

}
