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

package com.alibaba.cloud.integration.business.controller;

import com.alibaba.cloud.integration.business.entity.Order;
import com.alibaba.cloud.integration.common.Result;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static com.alibaba.cloud.integration.common.ResultEnum.COMMON_FAILED;

/**
 * @author TrevorLink
 */
@RestController
@RequestMapping("/order")
public class OrderBusinessController {

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/create")
	public Result<?> createOrder(@RequestParam("userId") String userId,
			@RequestParam("commodityCode") String commodityCode,
			@RequestParam("count") Integer count) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> createParamMap = new LinkedMultiValueMap<String, String>();
		createParamMap.add("userId", userId);
		createParamMap.add("commodityCode", commodityCode);
		createParamMap.add("count", count + "");

		HttpEntity<MultiValueMap<String, String>> createRequest = new HttpEntity<>(
				createParamMap, headers);

		ParameterizedTypeReference<Result<?>> typeReference = new ParameterizedTypeReference<Result<?>>() {
		};
		ResponseEntity<Result<?>> response = restTemplate.exchange(
				"http://localhost:8010/order/create", HttpMethod.POST, createRequest,
				typeReference);
		if (response.getBody().getCode().equals(COMMON_FAILED.getCode())) {
			return response.getBody();
		}
		String data = JSON.toJSONString(response.getBody().getData());
		Order order = JSONObject.parseObject(data, Order.class);
		ResponseEntity<Result<?>> res = restTemplate.exchange(
				"http://localhost:8010/order/query?orderId=" + order.getId(),
				HttpMethod.GET, null, typeReference);
		return res.getBody();
	}

}
