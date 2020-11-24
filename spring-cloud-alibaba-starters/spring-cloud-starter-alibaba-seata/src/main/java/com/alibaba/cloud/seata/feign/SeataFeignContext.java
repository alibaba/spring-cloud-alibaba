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

package com.alibaba.cloud.seata.feign;

import java.util.HashMap;
import java.util.Map;

import feign.Client;

import org.springframework.cloud.openfeign.FeignContext;

/**
 * @author xiaojing
 */
public class SeataFeignContext extends FeignContext {

	private final SeataFeignObjectWrapper SEATA_FEIGN_OBJECT_WRAPPER;

	private final FeignContext DELEGATE;

	SeataFeignContext(SeataFeignObjectWrapper seataFeignObjectWrapper,
			FeignContext delegate) {
		this.SEATA_FEIGN_OBJECT_WRAPPER = seataFeignObjectWrapper;
		this.DELEGATE = delegate;
	}

	@Override
	public <T> T getInstance(String name, Class<T> type) {
		T object = this.DELEGATE.getInstance(name, type);
		if (object instanceof Client) {
			return object;
		}
		return (T) this.SEATA_FEIGN_OBJECT_WRAPPER.wrap(object);
	}

	@Override
	public <T> Map<String, T> getInstances(String name, Class<T> type) {
		Map<String, T> instances = this.DELEGATE.getInstances(name, type);
		if (instances == null) {
			return null;
		}
		Map<String, T> convertedInstances = new HashMap<>();
		for (Map.Entry<String, T> entry : instances.entrySet()) {
			if (entry.getValue() instanceof Client) {
				convertedInstances.put(entry.getKey(), entry.getValue());
			}
			else {
				convertedInstances.put(entry.getKey(),
						(T) this.SEATA_FEIGN_OBJECT_WRAPPER.wrap(entry.getValue()));
			}
		}
		return convertedInstances;
	}

}
