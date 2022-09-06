package com.alibaba.cloud.governance.istio.util;

import com.alibaba.cloud.governance.common.matcher.HeaderMatcher;
import com.alibaba.cloud.governance.common.matcher.IpMatcher;
import com.alibaba.cloud.governance.common.matcher.MatcherType;
import com.alibaba.cloud.governance.common.matcher.StringMatcher;
import io.envoyproxy.envoy.config.core.v3.CidrRange;
import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;
import org.apache.commons.lang3.StringUtils;

public class ConvUtil {

	public static StringMatcher convStringMatcher(
			io.envoyproxy.envoy.type.matcher.v3.StringMatcher stringMatcher) {
		boolean isIgnoreCase = stringMatcher.getIgnoreCase();
		String exact = stringMatcher.getExact();
		String prefix = stringMatcher.getPrefix();
		String suffix = stringMatcher.getSuffix();
		String contains = stringMatcher.getContains();
		String regex = stringMatcher.getSafeRegex().getRegex();
		if (StringUtils.isNotBlank(exact)) {
			return new StringMatcher(exact, MatcherType.EXACT, isIgnoreCase);
		}
		if (StringUtils.isNotBlank(prefix)) {
			return new StringMatcher(prefix, MatcherType.PREFIX, isIgnoreCase);
		}
		if (StringUtils.isNotBlank(suffix)) {
			return new StringMatcher(suffix, MatcherType.SUFFIX, isIgnoreCase);
		}
		if (StringUtils.isNotBlank(contains)) {
			return new StringMatcher(contains, MatcherType.CONTAINS, isIgnoreCase);
		}
		if (StringUtils.isNotBlank(regex)) {
			return new StringMatcher(regex);
		}
		return null;
	}

	public static StringMatcher convStringMatcher(
			io.envoyproxy.envoy.config.route.v3.HeaderMatcher headerMatcher) {
		return convStringMatcher(headerMatch2StringMatch(headerMatcher));
	}

	public static IpMatcher convertIpMatcher(CidrRange cidrRange) {
		return new IpMatcher(cidrRange.getPrefixLen().getValue(),
				cidrRange.getAddressPrefix());
	}

	public static HeaderMatcher convertHeaderMatcher(
			io.envoyproxy.envoy.config.route.v3.HeaderMatcher headerMatcher) {
		return new HeaderMatcher(
				convStringMatcher(headerMatch2StringMatch(headerMatcher)));
	}

	private static io.envoyproxy.envoy.type.matcher.v3.StringMatcher headerMatch2StringMatch(
			io.envoyproxy.envoy.config.route.v3.HeaderMatcher headerMatcher) {
		if (headerMatcher == null) {
			return null;
		}
		if (headerMatcher.getPresentMatch()) {
			io.envoyproxy.envoy.type.matcher.v3.StringMatcher.Builder builder = io.envoyproxy.envoy.type.matcher.v3.StringMatcher
					.newBuilder();
			return builder.setSafeRegex(RegexMatcher.newBuilder().build())
					.setIgnoreCase(true).build();
		}
		if (!headerMatcher.hasStringMatch()) {
			io.envoyproxy.envoy.type.matcher.v3.StringMatcher.Builder builder = io.envoyproxy.envoy.type.matcher.v3.StringMatcher
					.newBuilder();
			String exactMatch = headerMatcher.getExactMatch();
			String containsMatch = headerMatcher.getContainsMatch();
			String prefixMatch = headerMatcher.getPrefixMatch();
			String suffixMatch = headerMatcher.getSuffixMatch();
			RegexMatcher safeRegex = headerMatcher.getSafeRegexMatch();
			if (!StringUtils.isEmpty(exactMatch)) {
				builder.setExact(exactMatch);
			}
			else if (!StringUtils.isEmpty(containsMatch)) {
				builder.setContains(containsMatch);
			}
			else if (!StringUtils.isEmpty(prefixMatch)) {
				builder.setPrefix(prefixMatch);
			}
			else if (!StringUtils.isEmpty(suffixMatch)) {
				builder.setSuffix(suffixMatch);
			}
			else if (safeRegex.isInitialized()) {
				builder.setSafeRegex(safeRegex);
			}
			return builder.setIgnoreCase(true).build();
		}
		return headerMatcher.getStringMatch();
	}

}
