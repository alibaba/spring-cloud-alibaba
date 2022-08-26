package com.alibaba.cloud.governance.common.rules.manager;

import com.alibaba.cloud.governance.common.rules.auth.JwtRule;
import com.alibaba.cloud.governance.common.rules.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jwt.JwtClaims;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JwtRuleManager {

	private static Map<String, JwtRule> jwtRules = new ConcurrentHashMap<>();

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
