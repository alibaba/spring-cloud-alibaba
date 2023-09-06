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

package com.alibaba.cloud.example.common.service;

import com.alibaba.cloud.example.common.entity.ResultHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class OrderDAO {

	@Autowired
	private OrderService orderService;

	public ResultHolder<String> buy(String rId, String pId, Integer number) {
		return orderService.buy(rId, pId, number);
	}

	@FeignClient(name = "storage")
	public interface OrderService {

		@GetMapping("/buy/")
		ResultHolder<String> buy(@RequestParam(name = "rId") String rId,
				@RequestParam(name = "id") String id,
				@RequestParam(name = "number") Integer number);

	}

}
