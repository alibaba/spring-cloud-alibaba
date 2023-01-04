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

package com.alibaba.cloud.examples;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cloud.commons.governance.event.LabelRoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.labelrouting.LabelRouteRule;
import com.alibaba.cloud.commons.governance.labelrouting.MatchService;
import com.alibaba.cloud.commons.governance.labelrouting.UnifiedRouteDataStructure;
import com.alibaba.cloud.commons.governance.labelrouting.rule.HeaderRule;
import com.alibaba.cloud.commons.governance.labelrouting.rule.RouteRule;
import com.alibaba.cloud.commons.governance.labelrouting.rule.UrlRule;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HH
 */
@SpringBootApplication
@EnableDiscoveryClient(autoRegister = true)
@EnableFeignClients
public class ConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}

	@FeignClient(name = "service-provider")
	public interface FeignService {

		/**
		 * Feign test.
		 * @return String
		 */
		@GetMapping("/test")
		String test();

	}

	@RestController
	public class Controller implements ApplicationContextAware {

		@Autowired
		private ApplicationContext applicationContext;

		@Autowired
		private ConsumerApplication.FeignService feignService;

		@Override
		public void setApplicationContext(ApplicationContext applicationContext)
				throws BeansException {
			this.applicationContext = applicationContext;
		}

		@GetMapping("/router-test")
		public String notFound() {
			return feignService.test();
		}

		@GetMapping("/add")
		public void getDataFromControlPlaneTest() {
			List<RouteRule> routeRules = new ArrayList<>();
			List<MatchService> matchServices = new ArrayList<>();

			UnifiedRouteDataStructure unifiedRouteDataStructure = new UnifiedRouteDataStructure();
			unifiedRouteDataStructure.setTargetService("service-provider");

			LabelRouteRule labelRouteData = new LabelRouteRule();
			labelRouteData.setDefaultRouteVersion("v1");

			RouteRule routeRule = new HeaderRule();
			routeRule.setType("header");
			routeRule.setCondition("=");
			routeRule.setKey("tag");
			routeRule.setValue("gray");
			RouteRule routeRule1 = new UrlRule.Parameter();
			routeRule1.setType("parameter");
			routeRule1.setCondition(">");
			routeRule1.setKey("id");
			routeRule1.setValue("10");
			RouteRule routeRule2 = new UrlRule.Path();
			routeRule2.setType("path");
			routeRule2.setCondition("=");
			routeRule2.setValue("/router-test");
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

			List<UnifiedRouteDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
			unifiedRouteDataStructureList.add(unifiedRouteDataStructure);
			applicationContext.publishEvent(new LabelRoutingDataChangedEvent(this,
					unifiedRouteDataStructureList));
		}

		@GetMapping("/update")
		public void updateDataFromControlPlaneTest() {
			List<RouteRule> routeRules = new ArrayList<>();
			List<MatchService> matchServices = new ArrayList<>();

			UnifiedRouteDataStructure unifiedRouteDataStructure = new UnifiedRouteDataStructure();
			unifiedRouteDataStructure.setTargetService("service-provider");

			LabelRouteRule labelRouteData = new LabelRouteRule();
			labelRouteData.setDefaultRouteVersion("v1");

			RouteRule routeRule = new HeaderRule();
			routeRule.setType("header");
			routeRule.setCondition("=");
			routeRule.setKey("tag");
			routeRule.setValue("gray");
			RouteRule routeRule1 = new UrlRule.Parameter();
			routeRule1.setType("parameter");
			routeRule1.setCondition(">");
			routeRule1.setKey("id");
			routeRule1.setValue("10");
			RouteRule routeRule2 = new UrlRule.Path();
			routeRule2.setType("path");
			routeRule2.setCondition("=");
			routeRule2.setValue("/router-test");
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

			List<UnifiedRouteDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
			unifiedRouteDataStructureList.add(unifiedRouteDataStructure);
			applicationContext.publishEvent(new LabelRoutingDataChangedEvent(this,
					unifiedRouteDataStructureList));
		}

	}

}
