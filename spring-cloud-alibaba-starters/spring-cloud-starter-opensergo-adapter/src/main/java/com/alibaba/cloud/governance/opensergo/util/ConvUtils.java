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

package com.alibaba.cloud.governance.opensergo.util;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.commons.matcher.StringMatcher;
import com.alibaba.cloud.commons.matcher.StringMatcherType;
import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.envoyproxy.envoy.config.route.v3.ClusterSpecifierPlugin;
import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;
import io.opensergo.proto.router.v1.ClusterFallbackConfig_ClusterConfig;

/**
 * @author panxiaojun233
 * @author <a href="m13201628570@163.com"></a>
 * @since 2.2.10-RC1
 */
public final class ConvUtils {

	private ConvUtils() {

	}

	public static String getOpenSergoHost(String endpoint) {
		if (StringUtils.isNotEmpty(endpoint)) {
			return endpoint.split(":")[0];
		}
		return null;
	}

	public static Integer getOpenSergoPort(String endpoint) {
		if (StringUtils.isNotEmpty(endpoint)) {
			String portStr = endpoint.split(":")[1];
			return Integer.valueOf(portStr);
		}
		return null;
	}

	public static ClusterFallbackConfig_ClusterConfig convFallbackClusterConfig(
			ClusterSpecifierPlugin clusterSpecifierPlugin)
			throws InvalidProtocolBufferException {
		Message defaultInstance = Internal
				.getDefaultInstance(ClusterFallbackConfig_ClusterConfig.class);
		return (ClusterFallbackConfig_ClusterConfig) defaultInstance.getParserForType()
				.parseFrom(clusterSpecifierPlugin.getExtension().getTypedConfig()
						.getValue());
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
			return new StringMatcher(exact, StringMatcherType.EXACT, isIgnoreCase);
		}
		if (StringUtils.isNotBlank(prefix)) {
			return new StringMatcher(prefix, StringMatcherType.PREFIX, isIgnoreCase);
		}
		if (StringUtils.isNotBlank(suffix)) {
			return new StringMatcher(suffix, StringMatcherType.SUFFIX, isIgnoreCase);
		}
		if (StringUtils.isNotBlank(contains)) {
			return new StringMatcher(contains, StringMatcherType.CONTAIN, isIgnoreCase);
		}
		if (StringUtils.isNotBlank(regex)) {
			return new StringMatcher(regex);
		}
		return null;
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
