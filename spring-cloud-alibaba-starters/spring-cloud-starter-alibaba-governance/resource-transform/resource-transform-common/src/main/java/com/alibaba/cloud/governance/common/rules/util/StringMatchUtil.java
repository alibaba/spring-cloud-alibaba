package com.alibaba.cloud.governance.common.rules.util;

import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class StringMatchUtil {

	private static final Logger log = LoggerFactory.getLogger(StringUtils.class);

	public static boolean matchStr(String str, StringMatcher matcher) {
		if (StringUtils.isEmpty(str)) {
			return false;
		}
		String exact = matcher.getExact();
		String contains = matcher.getContains();
		String prefix = matcher.getPrefix();
		String suffix = matcher.getSuffix();
		if (matcher.getIgnoreCase()) {
			str = StringUtils.toRootLowerCase(str);
			exact = StringUtils.toRootLowerCase(exact);
			contains = StringUtils.toRootLowerCase(contains);
			prefix = StringUtils.toRootLowerCase(prefix);
			suffix = StringUtils.toRootLowerCase(suffix);
		}
		// exact match
		if (!StringUtils.isEmpty(exact)) {
			return exact.equals(str);
		}
		// contains match
		if (!StringUtils.isEmpty(contains)) {
			return str.contains(contains);
		}
		// prefix match
		if (!StringUtils.isEmpty(prefix)) {
			return str.startsWith(prefix);
		}
		// suffix match
		if (!StringUtils.isEmpty(suffix)) {
			return str.endsWith(suffix);
		}
		// regex match
		if (matcher.hasSafeRegex()) {
			RegexMatcher regexMatcher = matcher.getSafeRegex();
			try {
				return Pattern.matches(regexMatcher.getRegex(), str);
			}
			catch (Exception e) {
				log.error("error on regex match", e);
				return false;
			}
		}

		return false;
	}

}
