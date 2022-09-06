package com.alibaba.cloud.governance.common.matcher;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

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
			str = StringUtils.toRootLowerCase(str);
			matcher = StringUtils.toRootLowerCase(str);
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
