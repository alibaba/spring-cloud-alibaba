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

package org.springframework.cloud.alicloud.ans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.alicloud.ans.test.AnsMockTest.hostInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.cloud.client.ServiceInstance;

import com.alibaba.ans.core.NamingService;
import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;

/**
 * @author xiaojing
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(NamingService.class)
public class AnsDiscoveryClientTests {

	private String host = "123.123.123.123";
	private int port = 8888;
	private String serviceName = "test-service";

	@Test
	public void testGetServers() throws Exception {

		ArrayList<Host> hosts = new ArrayList<>();

		HashMap<String, String> map = new HashMap<>();
		map.put("test-key", "test-value");
		map.put("secure", "true");

		hosts.add(hostInstance(serviceName, false, host, port, map));

		PowerMockito.mockStatic(NamingService.class);
		when(NamingService.getHosts(eq(serviceName))).thenReturn(hosts);

		AnsDiscoveryClient discoveryClient = new AnsDiscoveryClient();

		List<ServiceInstance> serviceInstances = discoveryClient
				.getInstances(serviceName);

		assertThat(serviceInstances.size()).isEqualTo(1);

		ServiceInstance serviceInstance = serviceInstances.get(0);

		assertThat(serviceInstance.getServiceId()).isEqualTo(serviceName);
		assertThat(serviceInstance.getHost()).isEqualTo(host);
		assertThat(serviceInstance.getPort()).isEqualTo(port);
		// assertThat(serviceInstance.isSecure()).isEqualTo(true);
		// ans doesn't support metadata
		assertThat(serviceInstance.getUri().toString())
				.isEqualTo(getUri(serviceInstance));
		// assertThat(serviceInstance.getMetadata().get("test-key")).isEqualTo("test-value");
		// ans doesn't support metadata

	}

	@Test
	public void testGetAllService() throws Exception {

		Set<String> subscribedServices = new HashSet<>();

		subscribedServices.add(serviceName + "1");
		subscribedServices.add(serviceName + "2");
		subscribedServices.add(serviceName + "3");

		PowerMockito.mockStatic(NamingService.class);
		when(NamingService.getDomsSubscribed()).thenReturn(subscribedServices);

		AnsDiscoveryClient discoveryClient = new AnsDiscoveryClient();
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
