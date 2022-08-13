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

package com.alibaba.cloud.integration.order.feign;

import com.alibaba.cloud.integration.common.Result;
import com.alibaba.cloud.integration.order.feign.dto.StorageDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author TrevorLink
 */
@FeignClient(name = "integrated-storage")
public interface StorageServiceFeignClient {

	@PostMapping("/storage/reduce-stock")
	Result<?> reduceStock(@RequestBody StorageDTO productReduceStockDTO);

	@PostMapping("/storage/get-stock")
	Result<?> getRemainCount(@RequestBody StorageDTO storageDTO);

}
