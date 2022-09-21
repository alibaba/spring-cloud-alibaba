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

package com.alibaba.cloud.governance.auth.rules.auth;

import java.util.Map;

import com.alibaba.cloud.governance.common.matcher.StringMatcher;
import com.alibaba.cloud.governance.common.rule.AndRule;

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
