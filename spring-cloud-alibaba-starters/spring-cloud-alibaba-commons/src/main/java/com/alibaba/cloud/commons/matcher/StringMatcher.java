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

package com.alibaba.cloud.commons.matcher;

import java.util.Locale;
import java.util.regex.Pattern;

import com.alibaba.cloud.commons.lang.StringUtils;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class StringMatcher implements Matcher {

	private String matcher;

	private StringMatcherType type;

	private boolean isIgnoreCase;

	private String regex;

	public StringMatcher() {

	}

	public StringMatcher(String regex) {
		this.regex = regex;
		this.type = StringMatcherType.REGEX;
	}

	public StringMatcher(String matcher, StringMatcherType type, boolean isIgnoreCase) {
		this.matcher = matcher;
		this.type = type;
		this.isIgnoreCase = isIgnoreCase;
	}

	public boolean match(Object obj) {
		if (!(obj instanceof String)) {
			return false;
		}
		String str = (String) obj;
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
		case CONTAIN:
			return str.contains(matcher);
		case REGEX:
			try {
				return Pattern.matches(regex, str);
			}
			catch (Exception e) {
				return false;
			}
		default:
			throw new UnsupportedOperationException(
					"unsupported string compare operation");
		}
	}

	public String getMatcher() {
		return matcher;
	}

	public void setMatcher(String matcher) {
		this.matcher = matcher;
	}

	public StringMatcherType getType() {
		return type;
	}

	public void setType(StringMatcherType type) {
		this.type = type;
	}

	public boolean isIgnoreCase() {
		return isIgnoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		isIgnoreCase = ignoreCase;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

}
