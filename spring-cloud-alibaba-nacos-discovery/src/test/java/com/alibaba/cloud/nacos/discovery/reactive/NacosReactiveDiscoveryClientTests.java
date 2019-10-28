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

package com.alibaba.cloud.nacos.discovery.reactive;

import java.util.Arrays;

import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.springframework.cloud.client.ServiceInstance;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:echooy.mxq@gmail.com">echooymxq</a>
 **/
@ExtendWith(MockitoExtension.class)
class NacosReactiveDiscoveryClientTests {

	@Mock
	private NacosServiceDiscovery serviceDiscovery;

	@Mock
	private ServiceInstance serviceInstance;

	@InjectMocks
	private NacosReactiveDiscoveryClient client;

	@Test
	void testGetInstances() throws NacosException {

		when(serviceDiscovery.getInstances("reactive-service"))
				.thenReturn(singletonList(serviceInstance));

		Flux<ServiceInstance> instances = this.client.getInstances("reactive-service");

		StepVerifier.create(instances).expectNextCount(1).expectComplete().verify();
	}

	@Test
	void testGetServices() throws NacosException {

		when(serviceDiscovery.getServices())
				.thenReturn(Arrays.asList("reactive-service1", "reactive-service2"));

		Flux<String> services = this.client.getServices();

		StepVerifier.create(services).expectNext("reactive-service1", "reactive-service2")
				.expectComplete().verify();
	}

}
