/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.sentinel.feign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import feign.Contract;
import feign.MethodMetadata;

/**
 *
 * Using static field {@link SentinelContractHolder#metadataMap} to hold
 * {@link MethodMetadata} data
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class SentinelContractHolder implements Contract {

	private final Contract delegate;

	/**
	 * map key is constructed by ClassFullName + configKey. configKey is constructed by
	 * {@link feign.Feign#configKey}
	 */
	public final static Map<String, MethodMetadata> metadataMap = new HashMap();

	public SentinelContractHolder(Contract delegate) {
		this.delegate = delegate;
	}

	@Override
	public List<MethodMetadata> parseAndValidatateMetadata(Class<?> targetType) {
		List<MethodMetadata> metadatas = delegate.parseAndValidatateMetadata(targetType);
		metadatas.forEach(metadata -> metadataMap
				.put(targetType.getName() + metadata.configKey(), metadata));
		return metadatas;
	}

}
