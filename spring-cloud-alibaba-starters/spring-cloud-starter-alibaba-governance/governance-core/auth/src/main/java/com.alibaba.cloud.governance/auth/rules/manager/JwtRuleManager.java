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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.governance.auth.rules.auth.JwtRule;
import com.alibaba.cloud.governance.auth.rules.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jwt.JwtClaims;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

public final class JwtRuleManager {

	private static Map<String, JwtRule> jwtRules = new ConcurrentHashMap<>();

	private JwtRuleManager() {

	}

	public static void addJwtRule(JwtRule jwtRule) {
		jwtRules.put(jwtRule.getName(), jwtRule);
	}

	public static void clear() {
		jwtRules.clear();
	}

	public static Pair<JwtClaims, Boolean> isValid(MultiValueMap<String, String> params,
			HttpHeaders headers) {
		for (JwtRule rule : jwtRules.values()) {
			Pair<JwtClaims, Boolean> jwtClaimsBooleanPair = JwtUtil.matchJwt(params,
					headers, rule);
			if (jwtClaimsBooleanPair.getRight()) {
				if (!StringUtils.isEmpty(rule.getOutputPayloadToHeader())) {
					// output
					headers.set(rule.getOutputPayloadToHeader(),
							Base64.getEncoder()
									.encodeToString(jwtClaimsBooleanPair.getLeft()
											.toJson().getBytes(StandardCharsets.UTF_8)));
				}
				return jwtClaimsBooleanPair;
			}
		}
		return Pair.of(null, false);
	}

	public static boolean isEmpty() {
		return jwtRules.isEmpty();
	}

}
