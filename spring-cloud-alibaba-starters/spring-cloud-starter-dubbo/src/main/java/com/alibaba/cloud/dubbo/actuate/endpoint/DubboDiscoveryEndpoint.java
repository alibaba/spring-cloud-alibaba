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

package com.alibaba.cloud.dubbo.actuate.endpoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.cloud.dubbo.registry.DubboCloudRegistry;
import com.alibaba.cloud.dubbo.registry.SpringCloudRegistryFactory;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.integration.RegistryDirectory;
import org.apache.dubbo.rpc.Invoker;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER_SIDE;
import static org.apache.dubbo.common.constants.CommonConstants.SIDE_KEY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Dubbo Rest Metadata {@link Endpoint}.
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">Theonefx</a>
 */
@Endpoint(id = "dubboRegistryDirectory")
public class DubboDiscoveryEndpoint {

	@ReadOperation(produces = APPLICATION_JSON_VALUE)
	public Object get() {
		DubboCloudRegistry registry = (DubboCloudRegistry) SpringCloudRegistryFactory
				.getRegistries().stream().filter(o -> o instanceof DubboCloudRegistry)
				.findFirst().orElse(null);

		if (registry == null) {
			return Collections.emptyMap();
		}

		Map<URL, Set<NotifyListener>> map = registry.getSubscribed();

		Map<String, List<String>> result = new HashMap<>();
		map.forEach((url, listeners) -> {
			String side = url.getParameter(SIDE_KEY);
			if (!CONSUMER_SIDE.equals(side)) {
				return;
			}

			List<String> list = listeners.stream()
					.filter(l -> l instanceof RegistryDirectory)
					.map(l -> (RegistryDirectory<?>) l)
					.map(RegistryDirectory::getAllInvokers).flatMap(List::stream)
					.map(Invoker::getUrl).map(URL::toServiceString).distinct().sorted()
					.collect(Collectors.toList());

			result.put(url.getServiceKey(), list);
		});
		return result;
	}

}
