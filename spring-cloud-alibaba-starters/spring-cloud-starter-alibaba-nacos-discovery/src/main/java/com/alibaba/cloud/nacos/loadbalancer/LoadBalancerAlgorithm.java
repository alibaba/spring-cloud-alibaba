/*
 * Copyright 2023-2024 the original author or authors.
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

package com.alibaba.cloud.nacos.loadbalancer;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.core.Ordered;

/**
 * Load Balancer algorithm interface.
 * When expanding the load balancing algorithm, implement this interface and register it as a bean.
 *
 * @author <a href="mailto:zhangbin1010@qq.com">zhangbinhub</a>
 */
public interface LoadBalancerAlgorithm extends Ordered {
	/**
	 * default service id.
	 */
	String DEFAULT_SERVICE_ID = "defaultServiceId";

	String getServiceId();

	ServiceInstance getInstance(Request<?> request, List<ServiceInstance> serviceInstances);
}
