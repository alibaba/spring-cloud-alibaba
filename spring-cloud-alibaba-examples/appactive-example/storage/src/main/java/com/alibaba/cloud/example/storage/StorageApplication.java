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

package com.alibaba.cloud.example.storage;

import com.alibaba.cloud.example.common.entity.ResultHolder;
import com.alibaba.cloud.example.storage.service.OrderServiceImpl;
import io.appactive.java.api.base.AppContextClient;
import io.appactive.support.log.LogUtil;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EntityScan("com.alibaba.cloud.example.*")
@ComponentScan(basePackages = { "com.alibaba.cloud.example" })
@RestController("/")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = { "com.alibaba.cloud.example" })
public class StorageApplication {

	private static final Logger logger = LogUtil.getLogger();

	@Autowired
	OrderServiceImpl orderService;

	@Value("${spring.application.name}")
	private String appName;

	public static void main(String[] args) {
		SpringApplication.run(StorageApplication.class, args);
	}

	/**
	 * buy1 is just for bypassing center service protection.
	 * @param rId rId
	 * @param id id
	 * @param number number
	 * @return resultHolder resultHolder
	 */
	@RequestMapping({ "/buy", "/buy1" })
	@ResponseBody
	public ResultHolder<String> buy(
			@RequestParam(required = false, defaultValue = "jack") String rId,
			@RequestParam(required = false, defaultValue = "12") String id,
			@RequestParam(required = false, defaultValue = "1") Integer number) {
		String routerId = AppContextClient.getRouteId();
		logger.info("buy, routerId: {}, pid: {}, number: {}", routerId, id, number);
		ResultHolder<String> resultHolder = orderService.buy(rId, id, number);
		resultHolder
				.setResult(String.format("routerId %s bought %d of item %s, result: %s",
						routerId, number, id, resultHolder.getResult()));
		return resultHolder;
	}

	@RequestMapping("/check")
	@ResponseBody
	public String check() {
		return "OK From " + appName;
	}

}
