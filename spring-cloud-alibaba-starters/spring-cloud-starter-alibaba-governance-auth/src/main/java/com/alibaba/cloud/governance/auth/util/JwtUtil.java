/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.governance.auth.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.cloud.commons.governance.auth.rule.JwtRule;
import com.alibaba.cloud.commons.lang.StringUtils;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public final class JwtUtil {

	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

	private static final String BEARER_PREFIX = "Bearer" + " ";

	private JwtUtil() {
	}

	public static String getTokenFromJwtRule(MultiValueMap<String, String> params,
			HttpHeaders headers, JwtRule jwtRule) {
		if (headers == null) {
			return StringUtils.EMPTY;
		}
		try {
			Map<String, String> jwtHeaders = jwtRule.getFromHeaders();
			if (!CollectionUtils.isEmpty(jwtHeaders)) {
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
			if (!CollectionUtils.isEmpty(fromParams)) {
				for (String fromParam : fromParams) {
					if (params.containsKey(fromParam)) {
						return params.getFirst(fromParam);
					}
				}
			}
			String token = headers.getFirst(HttpHeaders.AUTHORIZATION);
			if (StringUtils.isEmpty(token)) {
				return StringUtils.EMPTY;
			}
			if (token.startsWith(BEARER_PREFIX)) {
				return token.substring(BEARER_PREFIX.length());
			}
			return token;
		}
		catch (Exception e) {
			log.warn("No jwt token extracted from header or params");
		}
		return StringUtils.EMPTY;
	}

	public static JwtClaims extractJwtClaims(JwtRule jwtRule, String token) {
		String jwks = jwtRule.getJwks();
		if (jwks == null || jwks.isEmpty()) {
			return null;
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
				return null;
			}

			if (jwtRule.getAudiences() == null || jwtRule.getAudiences().isEmpty()) {
				return jwtContext.getJwtClaims();
			}

			Set<String> acceptAud = new HashSet<>(jwtRule.getAudiences());
			for (String aud : audiences) {
				if (acceptAud.contains(aud)) {
					return jwtContext.getJwtClaims();
				}
			}
			return null;
		}
		catch (JoseException e) {
			log.warn("Invalid jws from rule {}", jwtRule);
		}
		catch (InvalidJwtException e) {
			log.warn("Invalid jwt token {} for rule {}", token, jwtRule);
		}
		catch (MalformedClaimException e) {
			log.warn("Invalid jwt claims for rule {}", jwtRule);
		}
		return null;
	}

}
