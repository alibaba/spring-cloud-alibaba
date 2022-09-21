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

package com.alibaba.cloud.governance.auth.rules.auth;

import java.util.Map;

import com.alibaba.cloud.governance.common.matcher.HeaderMatcher;
import com.alibaba.cloud.governance.common.rule.AndRule;

public class HttpHeaderRule {

	private String name;

	private Map<String, AndRule<HeaderMatcher>> headers;

	public HttpHeaderRule(String name, Map<String, AndRule<HeaderMatcher>> headers) {
		this.name = name;
		this.headers = headers;
	}

	public String getName() {
		return name;
	}

	public Map<String, AndRule<HeaderMatcher>> getHeaders() {
		return headers;
	}

}
