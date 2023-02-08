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

package com.alibaba.cloud.commons.governance.auth.condition;

import com.alibaba.cloud.commons.matcher.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class AuthCondition {

	public enum ValidationType {

		/**
		 * All types of auth validation.
		 */
		HEADER, SOURCE_IP, REMOTE_IP, DEST_IP, REQUEST_PRINCIPALS, AUTH_AUDIENCES, AUTH_CLAIMS, AUTH_PRESENTERS, HOSTS, PATHS, PORTS, METHODS, IDENTITY

	}

	private static final Logger log = LoggerFactory.getLogger(AuthCondition.class);

	private ValidationType type;

	private String key;

	private Matcher matcher;

	public AuthCondition() {

	}

	public AuthCondition(ValidationType type, Matcher matcher) {
		this.type = type;
		this.matcher = matcher;
	}

	public AuthCondition(ValidationType type, String key, Matcher matcher) {
		this(type, matcher);
		this.key = key;
	}

	public ValidationType getType() {
		return type;
	}

	public void setType(ValidationType type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Matcher getMatcher() {
		return matcher;
	}

	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

}
