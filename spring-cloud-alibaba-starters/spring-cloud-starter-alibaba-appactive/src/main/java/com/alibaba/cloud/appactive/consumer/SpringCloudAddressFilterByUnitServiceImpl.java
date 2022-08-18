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

package com.alibaba.cloud.appactive.consumer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.alibaba.cloud.appactive.util.BaseUtil;
import io.appactive.java.api.base.enums.MiddleWareTypeEnum;
import io.appactive.rpc.base.consumer.RPCAddressFilterByUnitServiceImpl;
import io.appactive.support.lang.CollectionUtils;
import io.appactive.support.log.LogUtil;
import org.slf4j.Logger;

import org.springframework.util.AntPathMatcher;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
public class SpringCloudAddressFilterByUnitServiceImpl<T>
		extends RPCAddressFilterByUnitServiceImpl<T> {

	private static final Logger logger = LogUtil.getLogger();

	private static final String EMPTY_MATCHER = "";

	private final AntPathMatcher antPathMatcher;

	private final Map<String, String> URI_TO_BM = new ConcurrentHashMap<>();

	public SpringCloudAddressFilterByUnitServiceImpl(
			MiddleWareTypeEnum middleWareTypeEnum) {
		super(middleWareTypeEnum);
		this.antPathMatcher = new AntPathMatcher();
	}

	public static String calcBestMatcher(AntPathMatcher antPathMatcher,
			Set<String> candidates, String nameOfTarget) {
		// org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#lookupHandler(java.lang.String,
		// javax.servlet.http.HttpServletRequest)
		String bestMatcher = EMPTY_MATCHER;
		List<String> matchingPatterns = new ArrayList<>();
		String targetUri = BaseUtil.getUriFromPrimaryName(nameOfTarget);
		Set<String> candidateUris = candidates.stream()
				.map(BaseUtil::getUriFromPrimaryName).collect(Collectors.toSet());
		for (String candidateUri : candidateUris) {
			if (antPathMatcher.match(candidateUri, targetUri)) {
				matchingPatterns.add(candidateUri);
			}
		}
		Comparator<String> patternComparator = antPathMatcher
				.getPatternComparator(targetUri);
		if (!matchingPatterns.isEmpty()) {
			matchingPatterns.sort(patternComparator);
			if (logger.isDebugEnabled()) {
				logger.debug("Matching patterns for request [" + targetUri + "] are "
						+ matchingPatterns);
			}
			bestMatcher = matchingPatterns.get(0);
		}
		bestMatcher = BaseUtil.buildServicePrimaryName(
				BaseUtil.getAppNameFromPrimaryName(nameOfTarget), bestMatcher);
		return bestMatcher;
	}

	private String getBestMatcher(Set<String> candidates, String nameOfTarget) {
		String bestMatcher = EMPTY_MATCHER;
		if (CollectionUtils.isEmpty(candidates)) {
			emptyCache(null, nameOfTarget);
			return bestMatcher;
		}
		if (URI_TO_BM.containsKey(nameOfTarget)) {
			return URI_TO_BM.get(nameOfTarget);
		}
		bestMatcher = calcBestMatcher(antPathMatcher, candidates, nameOfTarget);
		if (!EMPTY_MATCHER.equals(bestMatcher)) {
			// only store it when it`s not empty
			URI_TO_BM.put(nameOfTarget, bestMatcher);
		}
		return bestMatcher;

	}

	@Override
	public List<T> addressFilter(String providerAppName, String servicePrimaryName,
			String routeId) {
		Set<String> candidates = getCachedServicePrimaryNames();
		/// in fact, pre-confined meta usually doesn`t contain a specific uri, it`s better
		/// to just skip testing and calculate matcher
		// String bestMatcher = servicePrimaryName;
		// if (!candidates.contains(servicePrimaryName)){
		// bestMatcher = bestMatcher(candidates, servicePrimaryName);
		// }
		String bestMatcher = getBestMatcher(candidates, servicePrimaryName);
		logger.info("candidates {}, servicePrimaryName {}, bestMatcher {}", candidates,
				servicePrimaryName, bestMatcher);
		return super.addressFilter(providerAppName, bestMatcher, routeId);
	}

	@Override
	public Boolean emptyCache(String providerAppName, String servicePrimaryName) {
		super.emptyCache(providerAppName, servicePrimaryName);
		URI_TO_BM.remove(servicePrimaryName);
		return true;
	}

}
