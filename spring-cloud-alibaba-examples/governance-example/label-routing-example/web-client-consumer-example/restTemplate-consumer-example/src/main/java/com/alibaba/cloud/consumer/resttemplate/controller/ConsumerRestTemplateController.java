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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.commons.governance.event.RoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import com.alibaba.cloud.consumer.resttemplate.interceptor.ConsumerRestRequestInterceptor;
import com.alibaba.cloud.routing.consumer.constants.ConsumerConstants;
import com.alibaba.cloud.routing.consumer.converter.Converter;
import com.alibaba.cloud.routing.consumer.entity.ConsumerNodeInfo;
import com.alibaba.cloud.routing.consumer.util.ReadJsonFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
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

	@Autowired
	private Converter<String, List<UnifiedRoutingDataStructure>> jsonConverter;

	private static String addRoutingRulePath;

	private static String updateRoutingRulePath;

	static {
		org.springframework.core.io.Resource resource1 = new ClassPathResource(
				"add-routing-rule.json");
		org.springframework.core.io.Resource resource2 = new ClassPathResource(
				"update-routing-rule.json");

		try {
			addRoutingRulePath = resource1.getFile().getPath();
			updateRoutingRulePath = resource2.getFile().getPath();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

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
					nmap.put(ConsumerConstants.PORT,
							Collections.singletonList(instance.getPort() + ""));
					nmap.put(ConsumerConstants.HOST,
							Collections.singletonList(instance.getHost()));
					nmap.put(ConsumerConstants.INSTANCE_ID,
							Collections.singletonList(instance.getInstanceId()));
					metaList.add(nmap);

					ConsumerNodeInfo.set(server, metaList);
				}
			}
		}

		return ConsumerNodeInfo.getNodeIno();
	}

	@GetMapping("/services")
	public Object getAllServices() {

		return discoveryClient.getServices();
	}

	@GetMapping("/router-test")
	public String routerTest() {

		return restTemplate.getForObject(
				ConsumerConstants.SERVICE_PROVIDER_ADDRESS + "/test-a1", String.class);
	}

	@GetMapping("/add")
	public void getDataFromControlPlaneTest() {

		log.info("Access /add routing rule interface, add routing rule..." + "\n"
				+ ConsumerConstants.ADD_RULE_DESCRIPTION);

		String content = ReadJsonFileUtils.convertFile2String(addRoutingRulePath);
		List<UnifiedRoutingDataStructure> unifiedRouteDataStructureList = jsonConverter
				.convert(content);

		applicationContext.publishEvent(
				new RoutingDataChangedEvent(this, unifiedRouteDataStructureList));

		log.info("Add routing rule success!");

	}

	@GetMapping("/update")
	public void updateDataFromControlPlaneTest() {

		log.info("Access /update routing rule interface, update routing rule..." + "\n"
				+ ConsumerConstants.UPDATE_RULE_DESCRIPTION);

		String content = ReadJsonFileUtils.convertFile2String(updateRoutingRulePath);
		List<UnifiedRoutingDataStructure> unifiedRouteDataStructureList = jsonConverter
				.convert(content);

		applicationContext.publishEvent(
				new RoutingDataChangedEvent(this, unifiedRouteDataStructureList));

		log.info("Update routing rule success!");

	}

}
