package com.alibaba.cloud.governance.common.rules.util;

import com.alibaba.cloud.governance.common.rules.auth.JwtRule;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtHeader;
import org.apache.commons.lang3.StringUtils;
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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private static String getTokenFromJwtRule(MultiValueMap<String, String> params, HttpHeaders headers, JwtRule jwtRule) {
        try {
            List<JwtHeader> jwtHeaders = jwtRule.getFromHeaders();
            if (jwtHeaders != null && !jwtHeaders.isEmpty()) {
                for (JwtHeader jwtHeader : jwtHeaders) {
                    if (headers.containsKey(jwtHeader.getName())) {
                        String token = headers.getFirst(jwtHeader.getName());
                        if (!StringUtils.isEmpty(token) && token.startsWith(jwtHeader.getValuePrefix())) {
                            return token.substring(jwtHeader.getValuePrefix().length());
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
            return Objects.requireNonNull(headers.getFirst(HttpHeaders.AUTHORIZATION)).substring(BEARER_PREFIX.length());
        } catch (Exception e) {
            log.error("unable to extract token from header or params", e);
        }
        return "";
    }

    public static JwtClaims matchJwt(MultiValueMap<String, String> params, HttpHeaders headers, JwtRule jwtRule) {
        String token = getTokenFromJwtRule(params, headers, jwtRule);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        String jwks = jwtRule.getJwks();
        if (jwks == null || jwks.isEmpty()) {
            return null;
        }
        JsonWebSignature jws = null;
        try {
            // don't validate jwt's attribute, just validate the sign
            JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder().setSkipAllValidators();
            jws = new JsonWebSignature();
            jws.setCompactSerialization(token);
            JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jwks);
            JwksVerificationKeyResolver jwksResolver = new JwksVerificationKeyResolver(jsonWebKeySet.getJsonWebKeys());
            jwtConsumerBuilder.setVerificationKeyResolver(jwksResolver);
            JwtConsumer jwtConsumer = jwtConsumerBuilder.build();
            JwtContext jwtContext = jwtConsumer.process(token);

            String issuer = jwtContext.getJwtClaims().getIssuer();
            List<String> audiences = jwtContext.getJwtClaims().getAudience();
            if (!StringUtils.isEmpty(jwtRule.getIssuer()) && !jwtRule.getIssuer().equals(issuer)) {
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
        } catch (JoseException e) {
            log.error("invalid jws", e);
        } catch (InvalidJwtException e) {
            log.warn("invalid jwt token {} for rule {}", token, jwtRule);
        } catch (MalformedClaimException e) {
            log.warn("invalid jwt claims for rule {}", jwtRule);
        }
        return null;
    }
}
