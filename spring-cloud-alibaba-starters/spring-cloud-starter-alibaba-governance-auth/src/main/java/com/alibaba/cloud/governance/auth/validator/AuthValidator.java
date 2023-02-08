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

package com.alibaba.cloud.governance.auth.validator;

import java.util.List;
import java.util.Map;

import com.alibaba.cloud.commons.governance.auth.condition.AuthCondition;
import com.alibaba.cloud.commons.governance.auth.rule.AuthRule;
import com.alibaba.cloud.commons.governance.auth.rule.JwtRule;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.commons.matcher.Matcher;
import com.alibaba.cloud.governance.auth.repository.AuthRepository;
import com.alibaba.cloud.governance.auth.util.JwtUtil;
import org.jose4j.jwt.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

/**
 * Use a abstract rule tree to validate the request. First, if the rules are all empty, we
 * just return true. Secondly, if any deny rule matches the request, we just return false.
 * Thirdly, if the allow rules are empty, we just return true. Last, if any allow rule
 * matches the request, we just return true, or we return false.
 *
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class AuthValidator {

	private static final Logger log = LoggerFactory.getLogger(AuthValidator.class);

	private final AuthRepository authRepository;

	public AuthValidator(AuthRepository authRepository) {
		this.authRepository = authRepository;
	}

	public boolean validate(UnifiedHttpRequest request) {
		if (request == null) {
			return false;
		}

		for (JwtRule jwtRule : authRepository.getJwtRules().values()) {
			// extract jwt token from request first
			String token = JwtUtil.getTokenFromJwtRule(request.getParams(),
					request.getHeaders(), jwtRule);
			if (!StringUtils.isEmpty(token)) {
				JwtClaims jwtClaims = JwtUtil.extractJwtClaims(jwtRule, token);
				if (jwtClaims == null) {
					return false;
				}
				request.jwtClaims = jwtClaims;
				break;
			}
		}

		Map<String, AuthRule> denyRules = authRepository.getDenyAuthRules();
		for (AuthRule denyRule : denyRules.values()) {
			if (validateRule(denyRule, request)) {
				return false;
			}
		}

		Map<String, AuthRule> allowRules = authRepository.getAllowAuthRules();

		if (allowRules.isEmpty()) {
			return true;
		}

		for (AuthRule allowRule : allowRules.values()) {
			if (validateRule(allowRule, request)) {
				return true;
			}
		}
		return false;
	}

	private boolean validateRule(AuthRule rule, UnifiedHttpRequest request) {
		if (rule.isLeaf()) {
			return validateLeafRule(rule, request);
		}

		List<AuthRule> authRules = rule.getChildren();
		boolean flag = rule.getOp() != AuthRule.RuleOperation.OR;
		for (AuthRule authRule : authRules) {
			if (rule.getOp() == AuthRule.RuleOperation.OR && flag) {
				break;
			}
			if (rule.getOp() == AuthRule.RuleOperation.AND && !flag) {
				break;
			}
			boolean childFlag = validateRule(authRule, request);
			switch (rule.getOp()) {
			case OR:
				flag |= childFlag;
				break;
			case AND:
				flag &= childFlag;
				break;
			}
		}
		// if "is not" is true, reverse the flag
		return rule.isNot() != flag;
	}

	private boolean validateLeafRule(AuthRule rule, UnifiedHttpRequest request) {
		try {
			AuthCondition condition = rule.getCondition();
			Matcher matcher = condition.getMatcher();
			String key = condition.getKey();
			if (matcher == null) {
				return false;
			}
			switch (condition.getType()) {
			case SOURCE_IP:
				return matcher.match(request.getSourceIp());
			case REMOTE_IP:
				return matcher.match(request.getRemoteIp());
			case DEST_IP:
				return matcher.match(request.getDestIp());
			// string
			case HOSTS:
				return matcher.match(request.getHost());
			case METHODS:
				return matcher.match(request.getMethod());
			case PATHS:
				return matcher.match(request.getPath());
			case REQUEST_PRINCIPALS:
			case AUTH_AUDIENCES:
			case AUTH_PRESENTERS:
				// if there is an AuthorizationPolicy but there is not a
				// RequestAuthentication, return false
				if (request.getJwtClaims() == null) {
					return false;
				}
				JwtClaims claims = request.getJwtClaims();
				switch (condition.getType()) {
				case REQUEST_PRINCIPALS:
					String issuer = claims.getIssuer();
					String subject = claims.getSubject();
					return matcher.match(issuer + "/" + subject);
				case AUTH_AUDIENCES:
					List<String> audiences = claims.getAudience();
					for (String audience : audiences) {
						if (matcher.match(audience)) {
							return true;
						}
					}
					return false;
				case AUTH_PRESENTERS:
					return matcher.match(claims.getClaimValueAsString("azp"));
				}
				return false;
			// int
			case PORTS:
				return matcher.match(request.getPort());
			// header
			case HEADER:
				HttpHeaders headers = request.getHeaders();
				if (!headers.containsKey(key)) {
					return false;
				}
				List<String> headerList = headers.get(key);
				if (headerList == null) {
					return false;
				}
				for (String header : headerList) {
					if (matcher.match(header)) {
						return true;
					}
				}
				return false;
			case AUTH_CLAIMS:
				claims = request.getJwtClaims();
				if (claims == null) {
					return false;
				}
				Object claimValue = claims.getClaimValue(key);

				if (claimValue instanceof List) {
					for (String claim : claims.getStringListClaimValue(key)) {
						if (matcher.match(claim)) {
							return true;
						}
					}
				}
				else {
					return matcher.match(claims.getStringClaimValue(key));
				}
				return false;
			}
		}
		catch (Exception e) {
			log.warn("Request {} doesn't match rule {}", request, rule);
		}
		return false;
	}

	public final static class UnifiedHttpRequest {

		private final String sourceIp;

		private final String destIp;

		private final String remoteIp;

		private final String host;

		private final int port;

		private final String method;

		private final String path;

		private final HttpHeaders headers;

		private final MultiValueMap<String, String> params;

		private JwtClaims jwtClaims;

		private UnifiedHttpRequest(String sourceIp, String destIp, String remoteIp,
				String host, int port, String method, String path, HttpHeaders headers,
				MultiValueMap<String, String> params) {
			this.sourceIp = sourceIp;
			this.destIp = destIp;
			this.remoteIp = remoteIp;
			this.host = host;
			this.port = port;
			this.method = method;
			this.path = path;
			this.headers = headers;
			this.params = params;
		}

		public String getSourceIp() {
			return sourceIp;
		}

		public String getDestIp() {
			return destIp;
		}

		public String getRemoteIp() {
			return remoteIp;
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

		public String getMethod() {
			return method;
		}

		public String getPath() {
			return path;
		}

		public HttpHeaders getHeaders() {
			return headers;
		}

		public MultiValueMap<String, String> getParams() {
			return params;
		}

		public JwtClaims getJwtClaims() {
			return jwtClaims;
		}

		public static class UnifiedHttpRequestBuilder {

			private String sourceIp;

			private String destIp;

			private String remoteIp;

			private String host;

			private int port;

			private String method;

			private String path;

			private HttpHeaders headers;

			private MultiValueMap<String, String> params;

			public UnifiedHttpRequestBuilder setSourceIp(String sourceIp) {
				this.sourceIp = sourceIp;
				return this;
			}

			public UnifiedHttpRequestBuilder setDestIp(String destIp) {
				this.destIp = destIp;
				return this;
			}

			public UnifiedHttpRequestBuilder setRemoteIp(String remoteIp) {
				this.remoteIp = remoteIp;
				return this;
			}

			public UnifiedHttpRequestBuilder setHost(String host) {
				this.host = host;
				return this;
			}

			public UnifiedHttpRequestBuilder setPort(int port) {
				this.port = port;
				return this;
			}

			public UnifiedHttpRequestBuilder setMethod(String method) {
				this.method = method;
				return this;
			}

			public UnifiedHttpRequestBuilder setPath(String path) {
				this.path = path;
				return this;
			}

			public UnifiedHttpRequestBuilder setHeaders(HttpHeaders headers) {
				this.headers = headers;
				return this;
			}

			public UnifiedHttpRequestBuilder setParams(
					MultiValueMap<String, String> params) {
				this.params = params;
				return this;
			}

			public UnifiedHttpRequest build() {
				return new UnifiedHttpRequest(sourceIp, destIp, remoteIp, host, port,
						method, path, headers, params);
			}

		}

	}

}
