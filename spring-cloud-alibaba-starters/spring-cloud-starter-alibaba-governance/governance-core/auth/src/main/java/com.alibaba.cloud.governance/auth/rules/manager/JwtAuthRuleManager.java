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

package com.alibaba.cloud.governance.auth.rules.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.governance.auth.rules.auth.JwtAuthRule;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JwtAuthRuleManager {

	private static final Logger log = LoggerFactory.getLogger(JwtAuthRuleManager.class);

	private static Map<String, JwtAuthRule> allowJwtAuthRules = new ConcurrentHashMap<>();

	private static Map<String, JwtAuthRule> denyJwtAuthRules = new ConcurrentHashMap<>();

	private JwtAuthRuleManager() {

	}

	public static void addJwtAuthRule(JwtAuthRule jwtAuthRule, boolean isAllow) {
		if (isAllow) {
			allowJwtAuthRules.put(jwtAuthRule.getName(), jwtAuthRule);
		}
		else {
			denyJwtAuthRules.put(jwtAuthRule.getName(), jwtAuthRule);
		}
	}

	public static void clear() {
		allowJwtAuthRules.clear();
		denyJwtAuthRules.clear();
	}

	public static boolean isValid(JwtClaims jwtClaims) {
		if (jwtClaims == null) {
			return false;
		}
		try {
			if (!denyJwtAuthRules.isEmpty() && judgeJwt(denyJwtAuthRules, jwtClaims)) {
				return false;
			}
			if (allowJwtAuthRules.isEmpty()) {
				return true;
			}
			return judgeJwt(allowJwtAuthRules, jwtClaims);
		}
		catch (MalformedClaimException e) {
			log.error("error on judge jwt", e);
			return false;
		}
	}

	private static boolean judgePresenter(String presenter, JwtAuthRule andRules) {
		return andRules == null || andRules.getAuthPresenters().isEmpty()
				|| andRules.getAuthPresenters().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(jwtPresenter -> jwtPresenter.match(presenter));
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgePrincipal(String subject, String issuer,
			JwtAuthRule andRules) {
		return andRules == null || andRules.getRequestPrincipals().isEmpty() || andRules
				.getRequestPrincipals().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream().anyMatch(
							jwtPrincipal -> jwtPrincipal.match(issuer + "/" + subject));
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgeAudience(List<String> audiences, JwtAuthRule andRules) {
		if (audiences == null || audiences.isEmpty()) {
			return andRules.getAuthAudiences().isEmpty();
		}
		return andRules == null || andRules.getAuthAudiences().isEmpty()
				|| andRules.getAuthAudiences().getRules().stream().allMatch(orRules -> {
					boolean flag = audiences.stream()
							.anyMatch(audStr -> orRules.getRules().stream()
									.anyMatch(jwtAudience -> jwtAudience.match(audStr)));
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgeClaims(JwtClaims jwtClaims, JwtAuthRule andRules) {
		return andRules == null || andRules.getAuthClaims().isEmpty()
				|| andRules.getAuthClaims().entrySet().stream().allMatch(allHeader -> {
					String key = allHeader.getKey();
					Object claimValue = jwtClaims.getClaimValue(key);
					List<String> claimList = new ArrayList<>();
					try {
						if (claimValue instanceof List) {
							claimList.addAll(jwtClaims.getStringListClaimValue(key));
						}
						else {
							claimList.add(jwtClaims.getStringClaimValue(key));
						}
					}
					catch (MalformedClaimException e) {
						log.error("invalid key type, unable to get key {} from jwtClaims",
								key);
					}
					return claimList.stream().anyMatch(claimStr -> allHeader.getValue()
							.getRules().stream().allMatch(andRule -> {
								// only support string header
								boolean flag = andRule.getRules().stream()
										.anyMatch(orRule -> orRule.match(claimStr));
								return andRule.isNot() != flag;
							}));
				});
	}

	private static boolean judgeJwt(Map<String, JwtAuthRule> rules, JwtClaims jwtClaims)
			throws MalformedClaimException {
		List<String> audiences = jwtClaims.getAudience();
		String issuer = jwtClaims.getIssuer();
		String subject = jwtClaims.getSubject();
		String presenter = jwtClaims.getClaimValueAsString("azp");
		return rules.values().stream()
				.anyMatch(andRules -> judgePresenter(presenter, andRules)
						&& judgePrincipal(subject, issuer, andRules)
						&& judgeAudience(audiences, andRules)
						&& judgeClaims(jwtClaims, andRules));
	}

	public static boolean isEmpty() {
		return allowJwtAuthRules.isEmpty() && denyJwtAuthRules.isEmpty();
	}

}
