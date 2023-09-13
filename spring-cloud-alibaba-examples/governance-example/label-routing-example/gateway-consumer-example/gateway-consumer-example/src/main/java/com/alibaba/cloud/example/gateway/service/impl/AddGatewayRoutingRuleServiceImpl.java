/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.example.gateway.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cloud.commons.governance.event.RoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.routing.MatchService;
import com.alibaba.cloud.commons.governance.routing.RoutingRule;
import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import com.alibaba.cloud.commons.governance.routing.rule.HeaderRoutingRule;
import com.alibaba.cloud.commons.governance.routing.rule.Rule;
import com.alibaba.cloud.commons.governance.routing.rule.UrlRoutingRule;
import com.alibaba.cloud.example.gateway.service.AddGatewayRoutingRuleService;
import com.alibaba.cloud.routing.gateway.common.GatewayConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * @author yuluo-yx
 * @author <a href="1481556636@qq.com"></a>
 */

@Service
public class AddGatewayRoutingRuleServiceImpl
		implements AddGatewayRoutingRuleService, ApplicationContextAware {

	private static final Logger log = LoggerFactory
			.getLogger(AddGatewayRoutingRuleServiceImpl.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {

		this.applicationContext = applicationContext;
	}

	@Override
	public void getDataFromControlPlaneTest() {

		log.info("请求 /add 接口，发布路由规则");

		List<Rule> routeRules = new ArrayList<>();
		List<MatchService> matchServices = new ArrayList<>();

		UnifiedRoutingDataStructure unifiedRouteDataStructure = new UnifiedRoutingDataStructure();

		// set target service
		unifiedRouteDataStructure
				.setTargetService(GatewayConstants.SERVICE_PROVIDER_NAME);

		RoutingRule labelRouteData = new RoutingRule();

		// set default service version
		labelRouteData.setDefaultRouteVersion("v1");

		// set request header routing rule
		Rule routeRule = new HeaderRoutingRule();
		routeRule.setCondition("=");
		routeRule.setKey("tag");
		routeRule.setValue("v2");

		// set request url routing rule
		Rule routeRule1 = new UrlRoutingRule.ParameterRoutingRule();
		routeRule1.setCondition(">");
		routeRule1.setKey("id");
		routeRule1.setValue("10");

		// set request url routing rule
		Rule routeRule2 = new UrlRoutingRule.PathRoutingRule();
		routeRule2.setCondition("=");
		routeRule2.setValue("/test-a1");

		// add routing rule to routeRules#List<Rule>
		routeRules.add(routeRule);
		routeRules.add(routeRule1);
		routeRules.add(routeRule2);

		// If the preceding conditions are met, the route is routed to the v2 instance and
		// the weight is set to 100
		MatchService matchService = new MatchService();
		matchService.setVersion("v2");
		matchService.setWeight(100);
		matchService.setRuleList(routeRules);
		matchServices.add(matchService);

		labelRouteData.setMatchRouteList(matchServices);

		unifiedRouteDataStructure.setLabelRouteRule(labelRouteData);

		List<UnifiedRoutingDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
		unifiedRouteDataStructureList.add(unifiedRouteDataStructure);

		RoutingDataChangedEvent routingDataChangedEvent = new RoutingDataChangedEvent(
				this, unifiedRouteDataStructureList);

		// Publish routing rules
		applicationContext.publishEvent(routingDataChangedEvent);

		log.info("请求 /add 接口，发布路由规则完成！");

	}

}
