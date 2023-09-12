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

package com.alibaba.cloud.consumer.resttemplate.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.commons.governance.event.RoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.routing.MatchService;
import com.alibaba.cloud.commons.governance.routing.RoutingRule;
import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import com.alibaba.cloud.commons.governance.routing.rule.HeaderRoutingRule;
import com.alibaba.cloud.commons.governance.routing.rule.Rule;
import com.alibaba.cloud.commons.governance.routing.rule.UrlRoutingRule;
import com.alibaba.cloud.consumer.constants.WebClientConsumerConstants;
import com.alibaba.cloud.consumer.entity.ConsumerNodeInfo;
import com.alibaba.cloud.consumer.resttemplate.interceptor.ConsumerRestRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author yuluo-yx
 * @author <a href="1481556636@qq.com"></a>
 */

@RestController
public class ConsumerRestTemplateController implements ApplicationContextAware {

	private static final Logger log = LoggerFactory
			.getLogger(ConsumerRestTemplateController.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private ConsumerRestRequestInterceptor consumerRestRequestInterceptor;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {

		this.applicationContext = applicationContext;
	}

	@GetMapping("/info4node")
	public Map<String, List<Map<String, List<String>>>> getNodeInfo() {

		String serverPort = consumerRestRequestInterceptor.getServerPort();

		List<String> services = discoveryClient.getServices();
		for (String service : services) {
			List<ServiceInstance> instances = discoveryClient.getInstances(service);
			for (ServiceInstance instance : instances) {
				if ((instance.getPort() + "").equals(serverPort)) {
					String server = instance.getServiceId();
					Map<String, String> metadata = instance.getMetadata();
					List<Map<String, List<String>>> metaList = new ArrayList<>();
					Map<String, List<String>> nmap = new HashMap<>();
					for (String s : metadata.keySet()) {
						nmap.put(s, Collections.singletonList(metadata.get(s)));
					}
					nmap.put(WebClientConsumerConstants.PORT,
							Collections.singletonList(instance.getPort() + ""));
					nmap.put(WebClientConsumerConstants.HOST,
							Collections.singletonList(instance.getHost()));
					nmap.put(WebClientConsumerConstants.INSTANCE_ID,
							Collections.singletonList(instance.getInstanceId()));
					metaList.add(nmap);

					ConsumerNodeInfo.set(server, metaList);
				}
			}
		}

		return ConsumerNodeInfo.getNodeIno();
	}

	@GetMapping("/service")
	public Object getAllServices() {

		return discoveryClient.getServices();
	}

	@GetMapping("/router-test")
	public String routerTest() {

		return restTemplate.getForObject(
				WebClientConsumerConstants.SERVICE_PROVIDER_ADDRESS + "/test-a1",
				String.class);
	}

	@GetMapping("/add")
	public void getDataFromControlPlaneTest() {

		log.info("请求 /add 接口，发布路由规则");

		List<Rule> routeRules = new ArrayList<>();
		List<MatchService> matchServices = new ArrayList<>();

		UnifiedRoutingDataStructure unifiedRouteDataStructure = new UnifiedRoutingDataStructure();

		// set target service
		unifiedRouteDataStructure
				.setTargetService(WebClientConsumerConstants.SERVICE_PROVIDER_NAME);

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
		routeRule2.setValue("/router-test");

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

	@GetMapping("/update")
	public void updateDataFromControlPlaneTest() {

		log.info("请求 /update 接口，更新路由规则");

		List<Rule> routeRules = new ArrayList<>();
		List<MatchService> matchServices = new ArrayList<>();

		UnifiedRoutingDataStructure unifiedRouteDataStructure = new UnifiedRoutingDataStructure();
		unifiedRouteDataStructure
				.setTargetService(WebClientConsumerConstants.SERVICE_PROVIDER_NAME);

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
		routeRule2.setValue("/router-test");
		routeRules.add(routeRule);
		routeRules.add(routeRule1);
		routeRules.add(routeRule2);

		// set weight 50
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

		log.info("请求 /update 接口，更新路由规则完成！");

	}

}
