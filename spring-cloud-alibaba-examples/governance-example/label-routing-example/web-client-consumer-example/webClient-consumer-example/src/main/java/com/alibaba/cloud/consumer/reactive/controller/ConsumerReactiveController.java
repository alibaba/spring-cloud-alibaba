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

package com.alibaba.cloud.consumer.reactive.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.cloud.commons.governance.event.RoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.routing.MatchService;
import com.alibaba.cloud.commons.governance.routing.RoutingRule;
import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import com.alibaba.cloud.commons.governance.routing.rule.HeaderRoutingRule;
import com.alibaba.cloud.commons.governance.routing.rule.Rule;
import com.alibaba.cloud.commons.governance.routing.rule.UrlRoutingRule;
import com.alibaba.cloud.consumer.constants.WebClientConsumerConstants;
import com.alibaba.cloud.consumer.entity.ConsumerNodeInfo;
import com.alibaba.cloud.consumer.reactive.configuration.ConsumerWebClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author yuluo-yx
 * @author <a href="1481556636@qq.com"></a>
 */

@RestController
public class ConsumerReactiveController implements ApplicationContextAware {

	private static final Logger log = LoggerFactory
			.getLogger(ConsumerReactiveController.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Resource
	private ReactiveDiscoveryClient reactiveDiscoveryClient;

	@Resource
	private WebClient.Builder webClientBuilder;

	@Resource
	private ConsumerWebClientConfiguration consumerWebClientConfiguration;

	@Autowired
	private DiscoveryClient discoveryClient;

	@GetMapping("/info4node")
	public Map<String, List<Map<String, List<String>>>> getInfo4Node() {

		String serverPort = consumerWebClientConfiguration.getServerPort();

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

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {

		this.applicationContext = applicationContext;
	}

	@GetMapping("/services")
	public Flux<String> getAllServices() {

		return reactiveDiscoveryClient
				.getInstances(WebClientConsumerConstants.SERVICE_PROVIDER_NAME)
				.map(serviceInstance -> serviceInstance.getHost() + ":"
						+ serviceInstance.getPort());
	}

	@GetMapping("/router-test")
	public Mono<String> routerTest() {

		return webClientBuilder.build().get()
				.uri(WebClientConsumerConstants.SERVICE_PROVIDER_ADDRESS + "/test-a1")
				.retrieve().bodyToMono(String.class);
	}

	@GetMapping("/add")
	public void getDataFromControlPlaneTest() {

		log.info("执行 /add 请求，添加路由规则！");

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

		log.info("/add 请求执行完毕，添加路由规则成功！");
	}

	@GetMapping("/update")
	public void updateDataFromControlPlaneTest() {
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

	}

}
