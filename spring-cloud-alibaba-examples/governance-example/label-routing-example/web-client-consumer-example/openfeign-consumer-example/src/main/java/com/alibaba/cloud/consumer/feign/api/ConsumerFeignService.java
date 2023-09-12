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

package com.alibaba.cloud.consumer.feign.api;

import com.alibaba.cloud.consumer.constants.WebClientConsumerConstants;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author yuluo-yx
 * @author <a href="1481556636@qq.com"></a>
 */

@FeignClient(name = WebClientConsumerConstants.SERVICE_PROVIDER_NAME)
public interface ConsumerFeignService {

	/**
	 * Feign test api.
	 * @return String type.
	 */
	@GetMapping("/test-a1")
	String routerTest();

}
