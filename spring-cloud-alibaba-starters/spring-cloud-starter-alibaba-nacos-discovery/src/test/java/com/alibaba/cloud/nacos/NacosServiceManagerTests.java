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

package com.alibaba.cloud.nacos;

import java.lang.reflect.Field;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link NacosServiceManager}.
 *
 * @author daguimu
 */
@ExtendWith(MockitoExtension.class)
public class NacosServiceManagerTests {

	@Mock
	private NamingService namingService;

	@Mock
	private NamingMaintainService namingMaintainService;

	private NacosServiceManager nacosServiceManager;

	@BeforeEach
	void setUp() throws Exception {
		nacosServiceManager = new NacosServiceManager();
		setField(nacosServiceManager, "namingService", namingService);
		setField(nacosServiceManager, "namingMaintainService", namingMaintainService);
	}

	@Test
	public void normalShutdownShouldSucceed() throws NacosException {
		nacosServiceManager.nacosServiceShutDown();

		verify(namingService).shutDown();
		verify(namingMaintainService).shutDown();
		assertThat(getField(nacosServiceManager, "namingService")).isNull();
		assertThat(getField(nacosServiceManager, "namingMaintainService")).isNull();
	}

	@Test
	public void namingServiceNpeShutdownShouldNotPropagate() throws NacosException {
		doThrow(new NullPointerException(
				"Cannot read field \"sharePublisher\" because \"com.alibaba.nacos.common.notify.NotifyCenter.INSTANCE\" is null"))
				.when(namingService).shutDown();

		nacosServiceManager.nacosServiceShutDown();

		verify(namingService).shutDown();
		verify(namingMaintainService).shutDown();
		assertThat(getField(nacosServiceManager, "namingService")).isNull();
		assertThat(getField(nacosServiceManager, "namingMaintainService")).isNull();
	}

	@Test
	public void namingMaintainServiceNpeShutdownShouldNotPropagate() throws NacosException {
		doThrow(new NullPointerException("NotifyCenter.INSTANCE is null"))
				.when(namingMaintainService).shutDown();

		nacosServiceManager.nacosServiceShutDown();

		verify(namingService).shutDown();
		verify(namingMaintainService).shutDown();
		assertThat(getField(nacosServiceManager, "namingService")).isNull();
		assertThat(getField(nacosServiceManager, "namingMaintainService")).isNull();
	}

	@Test
	public void bothServicesFailShouldStillCleanUp() throws NacosException {
		doThrow(new NullPointerException("INSTANCE is null"))
				.when(namingService).shutDown();
		doThrow(new NacosException(500, "shutdown error"))
				.when(namingMaintainService).shutDown();

		nacosServiceManager.nacosServiceShutDown();

		verify(namingService).shutDown();
		verify(namingMaintainService).shutDown();
		assertThat(getField(nacosServiceManager, "namingService")).isNull();
		assertThat(getField(nacosServiceManager, "namingMaintainService")).isNull();
	}

	@Test
	public void nacosExceptionDuringShutdownShouldNotPropagate() throws NacosException {
		doThrow(new NacosException(500, "server error"))
				.when(namingService).shutDown();

		nacosServiceManager.nacosServiceShutDown();

		verify(namingService).shutDown();
		verify(namingMaintainService).shutDown();
		assertThat(getField(nacosServiceManager, "namingService")).isNull();
	}

	@Test
	public void nullServicesShouldNotFail() throws NacosException {
		setField(nacosServiceManager, "namingService", null);
		setField(nacosServiceManager, "namingMaintainService", null);

		nacosServiceManager.nacosServiceShutDown();

		verify(namingService, never()).shutDown();
		verify(namingMaintainService, never()).shutDown();
	}

	private static void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Object getField(Object target, String fieldName) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(target);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
