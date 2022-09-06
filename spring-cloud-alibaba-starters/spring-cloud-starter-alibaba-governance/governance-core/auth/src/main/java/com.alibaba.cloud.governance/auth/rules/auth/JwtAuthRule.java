package com.alibaba.cloud.governance.auth.rules.auth;

import com.alibaba.cloud.governance.common.matcher.StringMatcher;
import com.alibaba.cloud.governance.common.rule.AndRule;

import java.util.Map;

public class JwtAuthRule {

	private String name;

	private AndRule<StringMatcher> requestPrincipals;

	private AndRule<StringMatcher> authAudiences;

	private Map<String, AndRule<StringMatcher>> authClaims;

	private AndRule<StringMatcher> authPresenters;

	public JwtAuthRule(String name, AndRule<StringMatcher> requestPrincipals,
			AndRule<StringMatcher> authAudiences,
			Map<String, AndRule<StringMatcher>> authClaims,
			AndRule<StringMatcher> authPresenters) {
		this.name = name;
		this.requestPrincipals = requestPrincipals;
		this.authAudiences = authAudiences;
		this.authClaims = authClaims;
		this.authPresenters = authPresenters;
	}

	public String getName() {
		return name;
	}

	public AndRule<StringMatcher> getRequestPrincipals() {
		return requestPrincipals;
	}

	public AndRule<StringMatcher> getAuthAudiences() {
		return authAudiences;
	}

	public Map<String, AndRule<StringMatcher>> getAuthClaims() {
		return authClaims;
	}

	public AndRule<StringMatcher> getAuthPresenters() {
		return authPresenters;
	}

}
