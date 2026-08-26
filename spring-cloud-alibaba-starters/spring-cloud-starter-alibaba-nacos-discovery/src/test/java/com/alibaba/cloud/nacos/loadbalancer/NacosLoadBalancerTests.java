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

package com.alibaba.cloud.nacos.loadbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.util.InetIPv6Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link NacosLoadBalancer} cross-cluster call log logic.
 *
 * @author daguimu
 */
@ExtendWith(MockitoExtension.class)
public class NacosLoadBalancerTests {

	@Mock
	private ObjectProvider<ServiceInstanceListSupplier> supplierProvider;

	@Mock
	private InetIPv6Utils inetIPv6Utils;

	@Mock
	private LoadBalancerAlgorithm defaultAlgorithm;

	private NacosDiscoveryProperties nacosDiscoveryProperties;

	private NacosLoadBalancer nacosLoadBalancer;

	@BeforeEach
	void setUp() {
		nacosDiscoveryProperties = new NacosDiscoveryProperties();

		Map<String, LoadBalancerAlgorithm> algorithmMap = new HashMap<>();
		algorithmMap.put(LoadBalancerAlgorithm.DEFAULT_SERVICE_ID, defaultAlgorithm);

		nacosLoadBalancer = new NacosLoadBalancer(
				supplierProvider,
				"test-service",
				nacosDiscoveryProperties,
				inetIPv6Utils,
				Collections.emptyList(),
				algorithmMap);
	}

	@Test
	public void blankClusterNameShouldNotWarnCrossCluster() {
		nacosDiscoveryProperties.setClusterName(null);
		List<ServiceInstance> instances = createInstances("192.168.1.1", "clusterA");

		when(defaultAlgorithm.getInstance(any(), any())).thenReturn(instances.get(0));

		Response<ServiceInstance> response = invokeGetInstanceResponse(instances);

		assertThat(response).isInstanceOf(DefaultResponse.class);
		assertThat(response.hasServer()).isTrue();
		assertThat(getLastWarnLogTime()).isEqualTo(0L);
	}

	@Test
	public void emptyClusterNameShouldNotWarnCrossCluster() {
		nacosDiscoveryProperties.setClusterName("");
		List<ServiceInstance> instances = createInstances("192.168.1.1", "clusterA");

		when(defaultAlgorithm.getInstance(any(), any())).thenReturn(instances.get(0));

		Response<ServiceInstance> response = invokeGetInstanceResponse(instances);

		assertThat(response).isInstanceOf(DefaultResponse.class);
		assertThat(response.hasServer()).isTrue();
		assertThat(getLastWarnLogTime()).isEqualTo(0L);
	}

	@Test
	public void sameClusterShouldSelectSameClusterInstance() {
		nacosDiscoveryProperties.setClusterName("clusterA");

		List<ServiceInstance> instances = new ArrayList<>();
		instances.add(createInstance("192.168.1.1", "clusterA"));
		instances.add(createInstance("192.168.1.2", "clusterB"));

		ServiceInstance clusterAInstance = instances.get(0);
		when(defaultAlgorithm.getInstance(any(), any())).thenReturn(clusterAInstance);

		Response<ServiceInstance> response = invokeGetInstanceResponse(instances);

		assertThat(response).isInstanceOf(DefaultResponse.class);
		assertThat(response.hasServer()).isTrue();
		assertThat(getLastWarnLogTime()).isEqualTo(0L);
	}

	@Test
	public void noSameClusterInstancesShouldStillReturnResponse() {
		nacosDiscoveryProperties.setClusterName("clusterC");

		List<ServiceInstance> instances = new ArrayList<>();
		instances.add(createInstance("192.168.1.1", "clusterA"));
		instances.add(createInstance("192.168.1.2", "clusterB"));

		when(defaultAlgorithm.getInstance(any(), any())).thenReturn(instances.get(0));

		Response<ServiceInstance> response = invokeGetInstanceResponse(instances);

		assertThat(response).isInstanceOf(DefaultResponse.class);
		assertThat(response.hasServer()).isTrue();
		assertThat(getLastWarnLogTime()).isGreaterThan(0L);
	}

	@Test
	public void crossClusterWarnShouldBeThrottled() {
		nacosDiscoveryProperties.setClusterName("clusterC");

		List<ServiceInstance> instances = new ArrayList<>();
		instances.add(createInstance("192.168.1.1", "clusterA"));

		when(defaultAlgorithm.getInstance(any(), any())).thenReturn(instances.get(0));

		// Attach a ListAppender to capture log output
		ch.qos.logback.classic.Logger nacosLogger = (ch.qos.logback.classic.Logger)
				org.slf4j.LoggerFactory.getLogger(NacosLoadBalancer.class);
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		listAppender.start();
		nacosLogger.addAppender(listAppender);

		try {
			for (int i = 0; i < 10; i++) {
				Response<ServiceInstance> response = invokeGetInstanceResponse(instances);
				assertThat(response).isInstanceOf(DefaultResponse.class);
				assertThat(response.hasServer()).isTrue();
			}

			// Warning should be emitted exactly once despite 10 invocations
			long crossClusterWarnCount = listAppender.list.stream()
					.filter(e -> e.getLevel() == Level.WARN)
					.filter(e -> e.getFormattedMessage().contains("cross-cluster call occurs"))
					.count();
			assertThat(crossClusterWarnCount).isEqualTo(1);
			assertThat(getLastWarnLogTime()).isGreaterThan(0L);
		}
		finally {
			nacosLogger.detachAppender(listAppender);
		}
	}

	@Test
	public void emptyServiceInstancesShouldReturnEmptyResponse() {
		Response<ServiceInstance> response = invokeGetInstanceResponse(Collections.emptyList());

		assertThat(response).isInstanceOf(EmptyResponse.class);
	}

	private Response<ServiceInstance> invokeGetInstanceResponse(List<ServiceInstance> instances) {
		try {
			java.lang.reflect.Method method = NacosLoadBalancer.class.getDeclaredMethod(
					"getInstanceResponse", Request.class, List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			Response<ServiceInstance> result = (Response<ServiceInstance>) method.invoke(
					nacosLoadBalancer, (Request<?>) null, instances);
			return result;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private long getLastWarnLogTime() {
		try {
			java.lang.reflect.Field field = NacosLoadBalancer.class.getDeclaredField("lastWarnLogTime");
			field.setAccessible(true);
			return (long) field.get(nacosLoadBalancer);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<ServiceInstance> createInstances(String host, String cluster) {
		List<ServiceInstance> instances = new ArrayList<>();
		instances.add(createInstance(host, cluster));
		return instances;
	}

	private ServiceInstance createInstance(String host, String cluster) {
		Map<String, String> metadata = new HashMap<>();
		metadata.put("nacos.cluster", cluster);
		return new DefaultServiceInstance("instance-" + host, "test-service", host, 8080, false, metadata);
	}
}
