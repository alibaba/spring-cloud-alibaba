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

package com.alibaba.cloud.governance.auth.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 */
public class AuthCondition {

	public enum ValidationType {

		/**
		 * All types of auth validation.
		 */
		HEADER, SOURCE_IP, REMOTE_IP, DEST_IP, REQUEST_PRINCIPALS, AUTH_AUDIENCES, AUTH_CLAIMS, AUTH_PRESENTERS, HOSTS, PATHS, PORTS, METHODS, IDENTITY

	}

	private static final Logger log = LoggerFactory.getLogger(AuthCondition.class);

	private final ValidationType type;

	private String key;

	private final Object matcher;

	public AuthCondition(ValidationType type, Object matcher) {
		this.type = type;
		this.matcher = matcher;
	}

	public AuthCondition(ValidationType type, String key, Object matcher) {
		this(type, matcher);
		this.key = key;
	}

	public ValidationType getType() {
		return type;
	}

	public String getKey() {
		return key;
	}

	public Object getMatcher() {
		return matcher;
	}

}
