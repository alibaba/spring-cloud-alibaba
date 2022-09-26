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

package com.alibaba.cloud.commons.governance.matcher;

import java.util.Locale;
import java.util.regex.Pattern;

import com.alibaba.cloud.commons.lang.StringUtils;

public class StringMatcher {

	private String matcher;

	private MatcherType type;

	private boolean isIgnoreCase;

	private String regex;

	public StringMatcher() {

	}

	public StringMatcher(String regex) {
		this.regex = regex;
		this.type = MatcherType.REGEX;
	}

	public StringMatcher(String matcher, MatcherType type, boolean isIgnoreCase) {
		this.matcher = matcher;
		this.type = type;
		this.isIgnoreCase = isIgnoreCase;
	}

	public MatcherType getType() {
		return type;
	}

	public String getMatcher() {
		return matcher;
	}

	public boolean match(String str) {
		if (StringUtils.isEmpty(str)) {
			return false;
		}
		if (isIgnoreCase) {
			str = str.toLowerCase(Locale.ROOT);
			matcher = matcher.toLowerCase(Locale.ROOT);
		}
		switch (type) {
		case EXACT:
			return str.equals(matcher);
		case PREFIX:
			return str.startsWith(matcher);
		case SUFFIX:
			return str.endsWith(matcher);
		case CONTAINS:
			return str.contains(matcher);
		case REGEX:
			try {
				return Pattern.matches(matcher, str);
			}
			catch (Exception e) {
				return false;
			}
		default:
			throw new UnsupportedOperationException(
					"unsupported string compare operation");
		}
	}

}
