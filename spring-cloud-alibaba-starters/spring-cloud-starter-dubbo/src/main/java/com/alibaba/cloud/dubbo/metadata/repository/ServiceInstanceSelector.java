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

package com.alibaba.cloud.dubbo.metadata.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.cloud.client.ServiceInstance;

/**
 * metadata service instance selector.
 *
 * @author <a href="mailto:liuxx-u@outlook.com">liuxx</a>
 */
public interface ServiceInstanceSelector {

	/**
	 * Select a service instance to get metadata.
	 * @param serviceInstances all service instance
	 * @return the service instance to get metadata
	 */
	Optional<ServiceInstance> select(List<ServiceInstance> serviceInstances);

}
