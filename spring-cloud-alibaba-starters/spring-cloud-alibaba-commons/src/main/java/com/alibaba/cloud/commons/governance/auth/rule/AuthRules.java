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

import java.util.Map;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class AuthRules {

	private final Map<String, AuthRule> allowAuthRules;

	private final Map<String, AuthRule> denyAuthRules;

	private final Map<String, JwtRule> jwtRules;

	public AuthRules(Map<String, AuthRule> allowAuthRules,
			Map<String, AuthRule> denyAuthRules, Map<String, JwtRule> jwtRules) {
		this.allowAuthRules = allowAuthRules;
		this.denyAuthRules = denyAuthRules;
		this.jwtRules = jwtRules;
	}

	public Map<String, AuthRule> getAllowAuthRules() {
		return allowAuthRules;
	}

	public Map<String, AuthRule> getDenyAuthRules() {
		return denyAuthRules;
	}

	public Map<String, JwtRule> getJwtRules() {
		return jwtRules;
	}

}
