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

package org.springframework.cloud.alibaba.nacos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;

import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.alibaba.nacos.test.NacosMockTest.serviceInstance;

/**
 * @author xiaojing
 */
public class NacosDiscoveryClientTests {

	private String host = "123.123.123.123";
	private int port = 8888;
	private String serviceName = "test-service";

	@Test
	public void testGetServers() throws Exception {

		ArrayList<Instance> instances = new ArrayList<>();

		HashMap<String, String> map = new HashMap<>();
		map.put("test-key", "test-value");
		map.put("secure", "true");

		instances.add(serviceInstance(serviceName, false, host, port, map));

		NacosDiscoveryProperties nacosDiscoveryProperties = mock(
				NacosDiscoveryProperties.class);

		NamingService namingService = mock(NamingService.class);

		when(nacosDiscoveryProperties.namingServiceInstance()).thenReturn(namingService);
		when(namingService.selectInstances(eq(serviceName), eq(true)))
				.thenReturn(instances);

		NacosDiscoveryClient discoveryClient = new NacosDiscoveryClient(
				nacosDiscoveryProperties);

		List<ServiceInstance> serviceInstances = discoveryClient
				.getInstances(serviceName);

		assertThat(serviceInstances.size()).isEqualTo(1);

		ServiceInstance serviceInstance = serviceInstances.get(0);

		assertThat(serviceInstance.getServiceId()).isEqualTo(serviceName);
		assertThat(serviceInstance.getHost()).isEqualTo(host);
		assertThat(serviceInstance.getPort()).isEqualTo(port);
		assertThat(serviceInstance.isSecure()).isEqualTo(true);
		assertThat(serviceInstance.getUri().toString())
				.isEqualTo(getUri(serviceInstance));
		assertThat(serviceInstance.getMetadata().get("test-key")).isEqualTo("test-value");

	}

	@Test
	public void testGetAllService() throws Exception {

		ListView<String> nacosServices = new ListView<>();

		nacosServices.setData(new LinkedList<>());

		nacosServices.getData().add(serviceName + "1");
		nacosServices.getData().add(serviceName + "2");
		nacosServices.getData().add(serviceName + "3");

		NacosDiscoveryProperties nacosDiscoveryProperties = mock(
				NacosDiscoveryProperties.class);

		NamingService namingService = mock(NamingService.class);

		NacosDiscoveryClient discoveryClient = new NacosDiscoveryClient(
				nacosDiscoveryProperties);

		when(nacosDiscoveryProperties.namingServiceInstance()).thenReturn(namingService);
		when(namingService.getServicesOfServer(eq(1), eq(Integer.MAX_VALUE)))
				.thenReturn(nacosServices);

		List<String> services = discoveryClient.getServices();

		assertThat(services.size()).isEqualTo(3);
		assertThat(services.contains(serviceName + "1"));
		assertThat(services.contains(serviceName + "2"));
		assertThat(services.contains(serviceName + "3"));

	}

	private String getUri(ServiceInstance instance) {

		if (instance.isSecure()) {
			return "https://" + host + ":" + port;
		}

		return "http://" + host + ":" + port;
	}
}
