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

package com.alibaba.cloud.commons.governance.auth.rule;

import java.util.List;
import java.util.Map;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class JwtRule {

	private String name;

	private Map<String, String> fromHeaders;

	private String issuer;

	private List<String> audiences;

	private String jwks;

	private List<String> fromParams;

	private String outputPayloadToHeader;

	private boolean forwardOriginalToken;

	public JwtRule() {

	}

	public JwtRule(String name, Map<String, String> fromHeaders, String issuer,
			List<String> audiences, String jwks, List<String> fromParams,
			String outputPayloadToHeader, boolean forwardOriginalToken) {
		this.name = name;
		this.fromHeaders = fromHeaders;
		this.issuer = issuer;
		this.audiences = audiences;
		this.jwks = jwks;
		this.fromParams = fromParams;
		this.outputPayloadToHeader = outputPayloadToHeader;
		this.forwardOriginalToken = forwardOriginalToken;
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getFromHeaders() {
		return fromHeaders;
	}

	public String getIssuer() {
		return issuer;
	}

	public List<String> getAudiences() {
		return audiences;
	}

	public String getJwks() {
		return jwks;
	}

	public List<String> getFromParams() {
		return fromParams;
	}

	public String getOutputPayloadToHeader() {
		return outputPayloadToHeader;
	}

	public boolean isForwardOriginalToken() {
		return forwardOriginalToken;
	}

}
