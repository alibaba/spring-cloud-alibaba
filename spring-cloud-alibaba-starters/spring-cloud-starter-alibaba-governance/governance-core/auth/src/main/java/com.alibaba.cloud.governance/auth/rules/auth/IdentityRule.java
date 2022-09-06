package com.alibaba.cloud.governance.auth.rules.auth;

import com.alibaba.cloud.governance.common.matcher.StringMatcher;
import com.alibaba.cloud.governance.common.rule.AndRule;

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
