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

package com.alibaba.cloud.governance.auth.rules.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.cloud.governance.auth.rules.auth.JwtRule;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

public final class JwtUtil {

	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

	private static final String BEARER_PREFIX = "Bearer ";

	private JwtUtil() {
	}

	private static String getTokenFromJwtRule(MultiValueMap<String, String> params,
			HttpHeaders headers, JwtRule jwtRule) {
		if (headers == null) {
			return "";
		}
		try {
			Map<String, String> jwtHeaders = jwtRule.getFromHeaders();
			if (jwtHeaders != null && !jwtHeaders.isEmpty()) {
				for (Map.Entry<String, String> entry : jwtHeaders.entrySet()) {
					String headerName = entry.getKey();
					String prefix = entry.getValue();
					if (headers.containsKey(headerName)) {
						String token = headers.getFirst(headerName);
						if (!StringUtils.isEmpty(token) && token.startsWith(prefix)) {
							return token.substring(prefix.length());
						}
					}
				}
			}
			List<String> fromParams = jwtRule.getFromParams();
			if (fromParams != null && !fromParams.isEmpty()) {
				for (String fromParam : fromParams) {
					if (params.containsKey(fromParam)) {
						return params.getFirst(fromParam);
					}
				}
			}
			return headers.getFirst(HttpHeaders.AUTHORIZATION)
					.substring(BEARER_PREFIX.length());
		}
		catch (Exception e) {
			log.error("unable to extract token from header or params");
		}
		return "";
	}

	public static Pair<JwtClaims, Boolean> matchJwt(MultiValueMap<String, String> params,
			HttpHeaders headers, JwtRule jwtRule) {
		String token = getTokenFromJwtRule(params, headers, jwtRule);
		// if the token is empty, return true
		if (StringUtils.isEmpty(token)) {
			return Pair.of(null, true);
		}
		String jwks = jwtRule.getJwks();
		if (jwks == null || jwks.isEmpty()) {
			return Pair.of(null, false);
		}
		JsonWebSignature jws = null;
		try {
			// don't validate jwt's attribute, just validate the sign
			JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder()
					.setSkipAllValidators();
			jws = new JsonWebSignature();
			jws.setCompactSerialization(token);
			JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jwks);
			JwksVerificationKeyResolver jwksResolver = new JwksVerificationKeyResolver(
					jsonWebKeySet.getJsonWebKeys());
			jwtConsumerBuilder.setVerificationKeyResolver(jwksResolver);
			JwtConsumer jwtConsumer = jwtConsumerBuilder.build();
			JwtContext jwtContext = jwtConsumer.process(token);

			String issuer = jwtContext.getJwtClaims().getIssuer();
			List<String> audiences = jwtContext.getJwtClaims().getAudience();
			if (!StringUtils.isEmpty(jwtRule.getIssuer())
					&& !jwtRule.getIssuer().equals(issuer)) {
				return Pair.of(null, false);
			}

			if (jwtRule.getAudiences() == null || jwtRule.getAudiences().isEmpty()) {
				return Pair.of(jwtContext.getJwtClaims(), true);
			}

			Set<String> acceptAud = new HashSet<>(jwtRule.getAudiences());
			for (String aud : audiences) {
				if (acceptAud.contains(aud)) {
					return Pair.of(jwtContext.getJwtClaims(), true);
				}
			}
			return Pair.of(null, false);
		}
		catch (JoseException e) {
			log.warn("invalid jws from rule {}", jwtRule);
		}
		catch (InvalidJwtException e) {
			log.warn("invalid jwt token {} for rule {}", token, jwtRule);
		}
		catch (MalformedClaimException e) {
			log.warn("invalid jwt claims for rule {}", jwtRule);
		}
		return Pair.of(null, false);
	}

}
