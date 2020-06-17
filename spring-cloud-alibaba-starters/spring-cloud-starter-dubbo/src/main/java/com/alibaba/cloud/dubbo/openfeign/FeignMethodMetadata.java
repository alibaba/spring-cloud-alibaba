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

package com.alibaba.cloud.dubbo.openfeign;

import java.lang.reflect.Method;

import com.alibaba.cloud.dubbo.metadata.RestMethodMetadata;
import org.apache.dubbo.rpc.service.GenericService;

/**
 * Feign {@link Method} Metadata.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
class FeignMethodMetadata {

	private final GenericService dubboGenericService;

	private final RestMethodMetadata dubboRestMethodMetadata;

	private final RestMethodMetadata feignMethodMetadata;

	FeignMethodMetadata(GenericService dubboGenericService,
			RestMethodMetadata dubboRestMethodMetadata,
			RestMethodMetadata feignMethodMetadata) {
		this.dubboGenericService = dubboGenericService;
		this.dubboRestMethodMetadata = dubboRestMethodMetadata;
		this.feignMethodMetadata = feignMethodMetadata;
	}

	GenericService getDubboGenericService() {
		return dubboGenericService;
	}

	RestMethodMetadata getDubboRestMethodMetadata() {
		return dubboRestMethodMetadata;
	}

	RestMethodMetadata getFeignMethodMetadata() {
		return feignMethodMetadata;
	}

}
