package com.alibaba.cloud.istio.util;

import com.alibaba.cloud.istio.rules.auth.JwtRule;
import com.alibaba.fastjson.JSON;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtHeader;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.VerificationJwkSelector;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Objects;

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
            // validate jwt's attribute
            JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder();
            if (!StringUtils.isEmpty(jwtRule.getIssuer())) {
                jwtConsumerBuilder.setExpectedIssuer(jwtRule.getIssuer());
            }
            if (jwtRule.getAudiences() != null && !jwtRule.getAudiences().isEmpty()) {
                jwtConsumerBuilder.setExpectedAudience(jwtRule.getAudiences().toArray(new String[0]));
            }
            JwtConsumer jwtConsumer = jwtConsumerBuilder.build();
            JwtClaims claims = jwtConsumer.processToClaims(token);
            // validate sign
            jws = new JsonWebSignature();
            jws.setCompactSerialization(token);
            JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(JSON.toJSONString(jwks));
            VerificationJwkSelector jwkSelector = new VerificationJwkSelector();
            JsonWebKey jwk = jwkSelector.select(jws, jsonWebKeySet.getJsonWebKeys());
            jws.setKey(jwk.getKey());
            boolean isValid = jws.verifySignature();
            return isValid ? claims : null;
        } catch (JoseException e) {
            log.error("invalid jws", e);
            return null;
        } catch (InvalidJwtException e) {
            log.warn("invalid jwt token {} for rule {}", token, jwtRule);
            return null;
        }
    }
}
