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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.appactive.common.ServiceMeta;
import com.alibaba.cloud.appactive.common.UriContext;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.fastjson.JSONObject;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PredicateKey;
import io.appactive.java.api.base.AppContextClient;
import io.appactive.java.api.base.constants.AppactiveConstant;
import io.appactive.java.api.base.constants.ResourceActiveType;
import io.appactive.java.api.rule.traffic.TrafficRouteRuleService;
import io.appactive.java.api.utils.lang.StringUtils;
import io.appactive.rule.ClientRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.AntPathMatcher;

/**
 * @author ChengPu raozihao
 */
public class AppactivePredicate extends AbstractServerPredicate {

	private static final Logger logger = LoggerFactory
			.getLogger(AppactivePredicate.class);

	private final TrafficRouteRuleService trafficRouteRuleService = ClientRuleService
			.getTrafficRouteRuleService();

	private final AntPathMatcher antPathMatcher = new AntPathMatcher();

	public AppactivePredicate(IRule rule, IClientConfig clientConfig) {
		super(rule, clientConfig);
	}

	public AppactivePredicate(IRule rule) {
		super(rule);
	}

	@Override
	public boolean apply(PredicateKey predicateKey) {
		// Just support Nacos Registry now, if it's a NacosServer, return true directly.
		if (!(predicateKey.getServer() instanceof NacosServer)) {
			return true;
		}
		NacosServer server = (NacosServer) predicateKey.getServer();

		// uriPath of the request.
		String uriPath = UriContext.getUriPath();
		Map<String, String> metadata = server.getMetadata();
		// zone
		String unitType = metadata.get("ut");
		String svcMeta = metadata.get("svc_meta");
		String version = metadata.get("svc_meta_v");
		if (unitType == null || svcMeta == null || version == null) {
			return true;
		}
		String serviceType = null;
		List<ServiceMeta> serviceMetas = JSONObject.parseArray(svcMeta,
				ServiceMeta.class);
		Map<String, String> matchingPatterns = new HashMap<>();
		for (ServiceMeta sm : serviceMetas) {
			if (antPathMatcher.match(sm.getUriPrefix(), uriPath)) {
				matchingPatterns.put(sm.getUriPrefix(), sm.getRa());
			}
		}
		Comparator<String> patternComparator = antPathMatcher
				.getPatternComparator(uriPath);
		if (!matchingPatterns.isEmpty()) {
			List<String> urls = new ArrayList<>(matchingPatterns.keySet());
			urls.sort(patternComparator);
			serviceType = matchingPatterns.get(urls.get(0));
		}

		if (!StringUtils.isBlank(serviceType) && ResourceActiveType.CENTER_RESOURCE_TYPE
				.equalsIgnoreCase(serviceType)) {
			return AppactiveConstant.CENTER_FLAG.equalsIgnoreCase(unitType);
		}
		else if (!StringUtils.isBlank(serviceType)
				&& ResourceActiveType.UNIT_RESOURCE_TYPE.equalsIgnoreCase(serviceType)) {
			// routeId of the request.
			String routeId = AppContextClient.getRouteId();
			if (routeId == null) {
				return false;
			}

			// targetUnit setting by users, such as unit0,unit1,...,unitn.
			String targetUnitByRouteId = trafficRouteRuleService
					.getUnitByRouteId(routeId);
			return !StringUtils.isBlank(targetUnitByRouteId)
					&& targetUnitByRouteId.equalsIgnoreCase(unitType);
		}

		return true;
	}

}
