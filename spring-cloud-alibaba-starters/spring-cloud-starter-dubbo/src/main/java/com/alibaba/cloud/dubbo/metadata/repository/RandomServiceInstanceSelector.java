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
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.cloud.client.ServiceInstance;

import static java.util.Optional.of;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Random {@link ServiceInstanceSelector}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class RandomServiceInstanceSelector implements ServiceInstanceSelector {

	@Override
	public Optional<ServiceInstance> select(List<ServiceInstance> serviceInstances) {
		if (isEmpty(serviceInstances)) {
			return Optional.empty();
		}
		ThreadLocalRandom random = ThreadLocalRandom.current();
		return of(serviceInstances.get(random.nextInt(serviceInstances.size())));
	}

}
