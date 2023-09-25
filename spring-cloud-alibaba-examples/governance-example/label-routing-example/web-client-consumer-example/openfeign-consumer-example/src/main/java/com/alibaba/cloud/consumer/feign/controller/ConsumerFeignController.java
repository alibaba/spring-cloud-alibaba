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

package com.alibaba.cloud.consumer.feign.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.commons.governance.event.RoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import com.alibaba.cloud.consumer.constants.WebClientConsumerConstants;
import com.alibaba.cloud.consumer.converter.Converter;
import com.alibaba.cloud.consumer.entity.ConsumerNodeInfo;
import com.alibaba.cloud.consumer.feign.api.ConsumerFeignService;
import com.alibaba.cloud.consumer.util.ReadJsonFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo-yx
 * @author <a href="1481556636@qq.com"></a>
 */

@RestController
public class ConsumerFeignController implements ApplicationContextAware {

	private static final Logger log = LoggerFactory
			.getLogger(ConsumerFeignController.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ConsumerFeignService consumerFeignService;

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

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {

		this.applicationContext = applicationContext;
	}

	@GetMapping("/info4node")
	public Map<String, List<Map<String, List<String>>>> getInfo4Node() {

		return ConsumerNodeInfo.getNodeIno();
	}

	@GetMapping("/router-test")
	public String notFound() {

		return consumerFeignService.routerTest();
	}

	@GetMapping("/add")
	public void getDataFromControlPlaneTest() {

		log.info("Access /add routing rule interface, add routing rule..." + "\n"
				+ WebClientConsumerConstants.ADD_RULE_DESCRIPTION);

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
				+ WebClientConsumerConstants.UPDATE_RULE_DESCRIPTION);

		String content = ReadJsonFileUtils.convertFile2String(updateRoutingRulePath);
		List<UnifiedRoutingDataStructure> unifiedRouteDataStructureList = jsonConverter
				.convert(content);

		applicationContext.publishEvent(
				new RoutingDataChangedEvent(this, unifiedRouteDataStructureList));

		log.info("Update routing rule success!");

	}

}
