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

package com.alibaba.example.zuul.controller;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cloud.commons.governance.event.RoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.routing.MatchService;
import com.alibaba.cloud.commons.governance.routing.RoutingRule;
import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import com.alibaba.cloud.commons.governance.routing.rule.HeaderRoutingRule;
import com.alibaba.cloud.commons.governance.routing.rule.Rule;
import com.alibaba.cloud.commons.governance.routing.rule.UrlRoutingRule;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@RestController
public class RouteRuleController implements ApplicationContextAware {

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {

		this.applicationContext = applicationContext;
	}

	@GetMapping("/add")
	public String getDataFromControlPlaneTest() {
		List<Rule> routeRules = new ArrayList<>();
		List<MatchService> matchServices = new ArrayList<>();

		UnifiedRoutingDataStructure unifiedRouteDataStructure = new UnifiedRoutingDataStructure();
		unifiedRouteDataStructure.setTargetService("service-provider");

		RoutingRule labelRouteData = new RoutingRule();
		labelRouteData.setDefaultRouteVersion("v1");

		Rule routeRule = new HeaderRoutingRule();
		routeRule.setCondition("=");
		routeRule.setKey("tag");
		routeRule.setValue("v2");
		Rule routeRule1 = new UrlRoutingRule.ParameterRoutingRule();
		routeRule1.setCondition(">");
		routeRule1.setKey("id");
		routeRule1.setValue("10");
		Rule routeRule2 = new UrlRoutingRule.PathRoutingRule();
		routeRule2.setCondition("=");
		routeRule2.setValue("/service-provider/test-a1");
		routeRules.add(routeRule);
		routeRules.add(routeRule1);
		routeRules.add(routeRule2);

		MatchService matchService = new MatchService();
		matchService.setVersion("v2");
		matchService.setWeight(100);
		matchService.setRuleList(routeRules);
		matchServices.add(matchService);

		labelRouteData.setMatchRouteList(matchServices);

		unifiedRouteDataStructure.setLabelRouteRule(labelRouteData);

		List<UnifiedRoutingDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
		unifiedRouteDataStructureList.add(unifiedRouteDataStructure);
		applicationContext.publishEvent(
				new RoutingDataChangedEvent(this, unifiedRouteDataStructureList));

		return "添加路由规则完成！";
	}

	@GetMapping("/update")
	public String updateDataFromControlPlaneTest() {
		List<Rule> routeRules = new ArrayList<>();
		List<MatchService> matchServices = new ArrayList<>();

		UnifiedRoutingDataStructure unifiedRouteDataStructure = new UnifiedRoutingDataStructure();
		unifiedRouteDataStructure.setTargetService("service-provider");

		RoutingRule labelRouteData = new RoutingRule();
		labelRouteData.setDefaultRouteVersion("v1");

		Rule routeRule = new HeaderRoutingRule();
		routeRule.setCondition("=");
		routeRule.setKey("tag");
		routeRule.setValue("v2");
		Rule routeRule1 = new UrlRoutingRule.ParameterRoutingRule();
		routeRule1.setCondition(">");
		routeRule1.setKey("id");
		routeRule1.setValue("10");
		Rule routeRule2 = new UrlRoutingRule.PathRoutingRule();
		routeRule2.setCondition("=");
		routeRule2.setValue("/service-provider/test-a1");
		routeRules.add(routeRule);
		routeRules.add(routeRule1);
		routeRules.add(routeRule2);

		MatchService matchService = new MatchService();
		matchService.setVersion("v2");
		matchService.setWeight(50);
		matchService.setRuleList(routeRules);
		matchServices.add(matchService);

		labelRouteData.setMatchRouteList(matchServices);

		unifiedRouteDataStructure.setLabelRouteRule(labelRouteData);

		List<UnifiedRoutingDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
		unifiedRouteDataStructureList.add(unifiedRouteDataStructure);
		applicationContext.publishEvent(
				new RoutingDataChangedEvent(this, unifiedRouteDataStructureList));

		return "更新路由规则完成";
	}

}
