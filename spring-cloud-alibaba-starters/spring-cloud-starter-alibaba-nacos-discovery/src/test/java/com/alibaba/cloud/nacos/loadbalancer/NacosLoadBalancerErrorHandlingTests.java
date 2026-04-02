/*
 * Copyright 2013-present the original author or authors.
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

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.util.InetIPv6Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

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
 * Tests for {@link NacosLoadBalancer} error handling behavior.
 *
 * @author daguimu
 */
@ExtendWith(MockitoExtension.class)
public class NacosLoadBalancerErrorHandlingTests {

	@Mock
	private ObjectProvider<ServiceInstanceListSupplier> supplierProvider;

	@Mock
	private ServiceInstanceListSupplier serviceInstanceListSupplier;

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

	// ========== Tests via private getInstanceResponse (reflection) ==========

	@Test
	public void exceptionShouldReturnEmptyResponseNotNull() {
		when(defaultAlgorithm.getInstance(any(), any()))
				.thenThrow(new RuntimeException("simulated error"));

		Response<ServiceInstance> response = invokeGetInstanceResponse(createTestInstances());

		assertThat(response).isNotNull();
		assertThat(response).isInstanceOf(EmptyResponse.class);
		assertThat(response.hasServer()).isFalse();
	}

	@Test
	public void normalRequestShouldReturnValidResponse() {
		ServiceInstance instance = createTestInstance("i1", "192.168.1.1", 8080);
		List<ServiceInstance> instances = new ArrayList<>();
		instances.add(instance);

		when(defaultAlgorithm.getInstance(any(), any())).thenReturn(instance);

		Response<ServiceInstance> response = invokeGetInstanceResponse(instances);

		assertThat(response).isNotNull();
		assertThat(response.hasServer()).isTrue();
		assertThat(response.getServer()).isEqualTo(instance);
	}

	// ========== Tests via public choose() — end-to-end reactive path ==========

	@Test
	public void chooseShouldReturnEmptyResponseWhenAlgorithmThrows() {
		List<ServiceInstance> instances = createTestInstances();
		when(supplierProvider.getIfAvailable(any())).thenReturn(serviceInstanceListSupplier);
		when(serviceInstanceListSupplier.get(any())).thenReturn(Flux.just(instances));
		when(defaultAlgorithm.getInstance(any(), any()))
				.thenThrow(new RuntimeException("simulated algorithm failure"));

		StepVerifier.create(nacosLoadBalancer.choose(null))
				.assertNext(response -> {
					assertThat(response).isNotNull();
					assertThat(response).isInstanceOf(EmptyResponse.class);
					assertThat(response.hasServer()).isFalse();
				})
				.verifyComplete();
	}

	@Test
	public void chooseShouldReturnValidResponseOnNormalPath() {
		ServiceInstance instance = createTestInstance("i1", "192.168.1.1", 8080);
		List<ServiceInstance> instances = new ArrayList<>();
		instances.add(instance);
		when(supplierProvider.getIfAvailable(any())).thenReturn(serviceInstanceListSupplier);
		when(serviceInstanceListSupplier.get(any())).thenReturn(Flux.just(instances));
		when(defaultAlgorithm.getInstance(any(), any())).thenReturn(instance);

		StepVerifier.create(nacosLoadBalancer.choose(null))
				.assertNext(response -> {
					assertThat(response).isNotNull();
					assertThat(response).isInstanceOf(DefaultResponse.class);
					assertThat(response.hasServer()).isTrue();
					assertThat(response.getServer().getHost()).isEqualTo("192.168.1.1");
					assertThat(response.getServer().getPort()).isEqualTo(8080);
				})
				.verifyComplete();
	}

	@Test
	public void chooseShouldReturnEmptyResponseWhenNoInstances() {
		List<ServiceInstance> emptyInstances = Collections.emptyList();
		when(supplierProvider.getIfAvailable(any())).thenReturn(serviceInstanceListSupplier);
		when(serviceInstanceListSupplier.get(any())).thenReturn(Flux.just(emptyInstances));

		StepVerifier.create(nacosLoadBalancer.choose(null))
				.assertNext(response -> {
					assertThat(response).isNotNull();
					assertThat(response).isInstanceOf(EmptyResponse.class);
					assertThat(response.hasServer()).isFalse();
				})
				.verifyComplete();
	}

	// ========== Helper methods ==========

	private ServiceInstance createTestInstance(String instanceId, String host, int port) {
		Map<String, String> metadata = new HashMap<>();
		metadata.put("nacos.cluster", "clusterA");
		return new DefaultServiceInstance(instanceId, "test-service", host, port, false, metadata);
	}

	private List<ServiceInstance> createTestInstances() {
		List<ServiceInstance> instances = new ArrayList<>();
		instances.add(createTestInstance("i1", "192.168.1.1", 8080));
		return instances;
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
}
