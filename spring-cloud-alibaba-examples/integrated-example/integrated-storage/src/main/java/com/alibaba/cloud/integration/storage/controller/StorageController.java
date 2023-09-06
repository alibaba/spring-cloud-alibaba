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

package com.alibaba.cloud.integration.storage.controller;

import com.alibaba.cloud.integration.common.BusinessException;
import com.alibaba.cloud.integration.common.Result;
import com.alibaba.cloud.integration.storage.dto.StorageDTO;
import com.alibaba.cloud.integration.storage.service.StorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author TrevorLink
 */
@RestController
@RequestMapping("/storage")
public class StorageController {

	@Autowired
	private StorageService storageService;

	@PostMapping("/reduce-stock")
	public Result<?> reduceStock(@RequestBody StorageDTO storageDTO) {
		try {
			storageService.reduceStock(storageDTO.getCommodityCode(),
					storageDTO.getCount());
		}
		catch (BusinessException e) {
			return Result.failed(e.getMessage());
		}
		return Result.success("");
	}

	@GetMapping("/")
	public Result<?> getRemainCount(String commodityCode) {
		return storageService.getRemainCount(commodityCode);
	}

}
