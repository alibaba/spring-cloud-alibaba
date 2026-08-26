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

package com.alibaba.cloud.nacos.registry;

import java.util.Collections;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.cloud.client.serviceregistry.Registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:uuuyuqi@gmail.com">uuuyuqi</a>
 */
@ExtendWith(MockitoExtension.class)
public class NacosServiceRegistryTests {

	private static final String SERVICE_ID = "test-service";

	private static final String GROUP = "DEFAULT_GROUP";

	private static final String CLUSTER = "DEFAULT";

	private static final String HOST = "192.168.1.10";

	private static final int PORT = 8080;

	@Mock
	private NacosServiceManager nacosServiceManager;

	@Mock
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	@Mock
	private NamingService namingService;

	@Mock
	private Registration registration;

	private NacosServiceRegistry serviceRegistry;

	@BeforeEach
	void setUp() {
		when(nacosServiceManager.getNamingService()).thenReturn(namingService);
		when(nacosDiscoveryProperties.getGroup()).thenReturn(GROUP);
		when(nacosDiscoveryProperties.getClusterName()).thenReturn(CLUSTER);
		when(registration.getServiceId()).thenReturn(SERVICE_ID);
		when(registration.getHost()).thenReturn(HOST);
		when(registration.getPort()).thenReturn(PORT);
		serviceRegistry = new NacosServiceRegistry(nacosServiceManager,
				nacosDiscoveryProperties);
	}

	@Test
	public void downStatusShouldDeregisterInstance() throws NacosException {
		serviceRegistry.setStatus(registration, "DOWN");

		verify(namingService).deregisterInstance(SERVICE_ID, GROUP, HOST, PORT,
				CLUSTER);
		verify(namingService, never()).registerInstance(anyString(), anyString(),
				any(Instance.class));
	}

	@Test
	public void upStatusShouldRegisterInstance() throws NacosException {
		when(registration.getMetadata()).thenReturn(Collections.emptyMap());

		serviceRegistry.setStatus(registration, "UP");

		ArgumentCaptor<Instance> instanceCaptor = ArgumentCaptor
				.forClass(Instance.class);
		verify(namingService).registerInstance(eq(SERVICE_ID), eq(GROUP),
				instanceCaptor.capture());
		assertThat(instanceCaptor.getValue().getIp()).isEqualTo(HOST);
		assertThat(instanceCaptor.getValue().getPort()).isEqualTo(PORT);
		verify(namingService, never()).deregisterInstance(anyString(), anyString(),
				anyString(), anyInt(), anyString());
	}

}
