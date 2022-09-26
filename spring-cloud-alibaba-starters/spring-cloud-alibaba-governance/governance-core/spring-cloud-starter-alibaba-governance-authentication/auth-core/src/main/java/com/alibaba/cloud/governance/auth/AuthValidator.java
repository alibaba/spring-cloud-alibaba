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

package com.alibaba.cloud.governance.auth;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.commons.pair.Pair;
import com.alibaba.cloud.governance.auth.repository.AuthRepository;
import com.alibaba.cloud.governance.auth.rule.HttpHeaderRule;
import com.alibaba.cloud.governance.auth.rule.IpBlockRule;
import com.alibaba.cloud.governance.auth.rule.JwtAuthRule;
import com.alibaba.cloud.governance.auth.rule.JwtRule;
import com.alibaba.cloud.governance.auth.rule.TargetRule;
import com.alibaba.cloud.governance.auth.util.JwtUtil;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

public class AuthValidator {

	private static final Logger log = LoggerFactory.getLogger(AuthValidator.class);

	private AuthRepository authRepository;

	public AuthValidator(AuthRepository authRepository) {
		this.authRepository = authRepository;
	}

	public boolean validateHeader(HttpHeaders headers) {
		return this.isValidHeader(headers);
	}

	public boolean validateIp(String sourceIp, String destIp, String remoteIp) {
		return this.isValidIp(sourceIp, destIp, remoteIp);
	}

	public boolean validateJwtAuthRule(JwtClaims jwtClaims) {
		return this.isValidJwtRule(jwtClaims);
	}

	public Pair<JwtClaims, Boolean> validateJwt(MultiValueMap<String, String> params,
			HttpHeaders headers) {
		return this.isValidJwt(params, headers);
	}

	public boolean validateTargetRule(String host, int port, String method, String path) {
		return this.isValidTargetRule(host, port, method, path);
	}

	public boolean isEmptyJwtAuthRule() {
		return this.authRepository.getAuthData().getJwtAuthRuleManager()
				.getAllowJwtAuthRules().isEmpty()
				&& this.authRepository.getAuthData().getJwtAuthRuleManager()
						.getDenyJwtAuthRules().isEmpty();
	}

	public boolean isEmptyJwtRule() {
		return this.authRepository.getAuthData().getJwtRuleManager().getJwtRules()
				.isEmpty();
	}

	private boolean isValidHeader(HttpHeaders headers) {
		Map<String, HttpHeaderRule> denyHeaderRules = this.authRepository.getAuthData()
				.getHeaderRuleManager().getDenyHeaderRules();
		Map<String, HttpHeaderRule> allowHeaderRules = this.authRepository.getAuthData()
				.getHeaderRuleManager().getAllowHeaderRules();
		if (!denyHeaderRules.isEmpty() && judgeHttpHeaderRule(denyHeaderRules, headers)) {
			return false;
		}
		if (allowHeaderRules.isEmpty()) {
			return true;
		}
		return judgeHttpHeaderRule(allowHeaderRules, headers);
	}

	private boolean judgeHttpHeaderRule(Map<String, HttpHeaderRule> rules,
			HttpHeaders headers) {
		return rules.values().stream()
				.anyMatch(andRules -> judgeHttpHeaderRule(andRules, headers));
	}

	private boolean judgeHttpHeaderRule(HttpHeaderRule andRules, HttpHeaders headers) {
		return andRules == null || andRules.getHeaders().isEmpty()
				|| andRules.getHeaders().entrySet().stream().allMatch(allHeader -> {
					String key = allHeader.getKey();
					return allHeader.getValue().getRules().stream().allMatch(andRule -> {
						boolean flag = andRule.getRules().stream()
								.anyMatch(orRule -> orRule.match(headers, key));
						return andRule.isNot() != flag;
					});
				});
	}

	private boolean isValidIp(String sourceIp, String destIp, String remoteIp) {
		Map<String, IpBlockRule> denyIpBlockRules = this.authRepository.getAuthData()
				.getIpBlockRuleManager().getDenyIpBlockRules();
		Map<String, IpBlockRule> allowIpBlockRules = this.authRepository.getAuthData()
				.getIpBlockRuleManager().getAllowIpBlockRules();
		if (!denyIpBlockRules.isEmpty()
				&& judgeIpBlockRule(denyIpBlockRules, sourceIp, destIp, remoteIp)) {
			return false;
		}
		if (allowIpBlockRules.isEmpty()) {
			return true;
		}
		return judgeIpBlockRule(allowIpBlockRules, sourceIp, destIp, remoteIp);
	}

	private static boolean judgeIpBlockRule(Map<String, IpBlockRule> rule,
			String sourceIp, String destIp, String remoteIp) {
		return rule.values().stream()
				.allMatch(andRules -> judgeSourceIp(sourceIp, andRules)
						&& judgeDestIp(destIp, andRules)
						&& judgeRemoteIp(remoteIp, andRules));
	}

	private static boolean judgeSourceIp(String ip, IpBlockRule andRules) {
		return andRules.getSourceIps() == null || andRules.getSourceIps().isEmpty()
				|| andRules.getSourceIps().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpSourceIp -> httpSourceIp.match(ip));
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgeDestIp(String destIp, IpBlockRule andRules) {
		return andRules.getDestIps() == null || andRules.getDestIps().isEmpty()
				|| andRules.getDestIps().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpDestIp -> httpDestIp.match(destIp));
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgeRemoteIp(String remoteIp, IpBlockRule andRules) {
		return andRules.getRemoteIps() == null || andRules.getRemoteIps().isEmpty()
				|| andRules.getRemoteIps().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpRemoteIp -> httpRemoteIp.match(remoteIp));
					return orRules.isNot() != flag;
				});
	}

	private boolean isValidJwtRule(JwtClaims jwtClaims) {
		Map<String, JwtAuthRule> denyJwtAuthRules = this.authRepository.getAuthData()
				.getJwtAuthRuleManager().getDenyJwtAuthRules();
		Map<String, JwtAuthRule> allowJwtAuthRules = this.authRepository.getAuthData()
				.getJwtAuthRuleManager().getAllowJwtAuthRules();
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

	private boolean judgePresenter(String presenter, JwtAuthRule andRules) {
		return andRules == null || andRules.getAuthPresenters().isEmpty()
				|| andRules.getAuthPresenters().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(jwtPresenter -> jwtPresenter.match(presenter));
					return orRules.isNot() != flag;
				});
	}

	private boolean judgePrincipal(String subject, String issuer, JwtAuthRule andRules) {
		return andRules == null || andRules.getRequestPrincipals().isEmpty() || andRules
				.getRequestPrincipals().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream().anyMatch(
							jwtPrincipal -> jwtPrincipal.match(issuer + "/" + subject));
					return orRules.isNot() != flag;
				});
	}

	private boolean judgeAudience(List<String> audiences, JwtAuthRule andRules) {
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

	private boolean judgeClaims(JwtClaims jwtClaims, JwtAuthRule andRules) {
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

	private boolean judgeJwt(Map<String, JwtAuthRule> rules, JwtClaims jwtClaims)
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

	private Pair<JwtClaims, Boolean> isValidJwt(MultiValueMap<String, String> params,
			HttpHeaders headers) {
		Map<String, JwtRule> jwtRules = this.authRepository.getAuthData()
				.getJwtRuleManager().getJwtRules();
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

	private boolean isValidTargetRule(String host, int port, String method, String path) {
		Map<String, TargetRule> denyTargetRules = this.authRepository.getAuthData()
				.getTargetRuleManager().getDenyTargetRules();
		Map<String, TargetRule> allowTargetRules = this.authRepository.getAuthData()
				.getTargetRuleManager().getAllowTargetRules();
		if (!denyTargetRules.isEmpty()
				&& judgeTargetRule(denyTargetRules, host, port, method, path)) {
			return false;
		}
		if (allowTargetRules.isEmpty()) {
			return true;
		}
		return judgeTargetRule(allowTargetRules, host, port, method, path);
	}

	private boolean judgeHost(String host, TargetRule andRules) {
		return andRules.getHosts() == null || andRules.getHosts().isEmpty()
				|| andRules.getHosts().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpHost -> httpHost.match(host));
					return orRules.isNot() != flag;
				});
	}

	private boolean judgePort(int port, TargetRule andRules) {
		return andRules.getPorts() == null || andRules.getPorts().isEmpty()
				|| andRules.getPorts().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpPort -> port == httpPort);
					return orRules.isNot() != flag;
				});
	}

	private boolean judgeMethod(String method, TargetRule andRules) {
		return andRules.getMethods() == null || andRules.getMethods().isEmpty()
				|| andRules.getMethods().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpMethod -> httpMethod.match(method));
					return orRules.isNot() != flag;
				});
	}

	private boolean judgePath(String path, TargetRule andRules) {
		return andRules.getPaths() == null || andRules.getPaths().isEmpty()
				|| andRules.getPaths().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpPath -> httpPath.match(path));
					return orRules.isNot() != flag;
				});
	}

	private boolean judgeTargetRule(Map<String, TargetRule> rules, String host, int port,
			String method, String path) {
		return rules.values().stream()
				.anyMatch(andRules -> judgeHost(host, andRules)
						&& judgePort(port, andRules) && judgeMethod(method, andRules)
						&& judgePath(path, andRules));
	}

}
