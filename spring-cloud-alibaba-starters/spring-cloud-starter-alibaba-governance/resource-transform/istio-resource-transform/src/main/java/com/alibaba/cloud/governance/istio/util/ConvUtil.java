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

package com.alibaba.cloud.governance.istio.util;

import com.alibaba.cloud.governance.common.matcher.HeaderMatcher;
import com.alibaba.cloud.governance.common.matcher.IpMatcher;
import com.alibaba.cloud.governance.common.matcher.MatcherType;
import com.alibaba.cloud.governance.common.matcher.StringMatcher;
import io.envoyproxy.envoy.config.core.v3.CidrRange;
import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;
import org.apache.commons.lang3.StringUtils;

public final class ConvUtil {

	private ConvUtil() {

	}

	public static StringMatcher convStringMatcher(
			io.envoyproxy.envoy.type.matcher.v3.StringMatcher stringMatcher) {
		if (stringMatcher == null) {
			return null;
		}
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

	public static io.envoyproxy.envoy.type.matcher.v3.StringMatcher headerMatch2StringMatch(
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
