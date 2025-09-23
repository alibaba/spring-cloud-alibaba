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

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:uuuyuqi@gmail.com">uuuyuqi</a>
 */
@ExtendWith(MockitoExtension.class)
public class NacosGracefulShutdownDelegateTests {

	@Mock
	private NacosAutoServiceRegistration autoServiceRegistration;

	@Mock
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	@Mock
	private ApplicationContext applicationContext;

	@InjectMocks
	private NacosGracefulShutdownDelegate delegate;

	@BeforeEach
	void setUp() {
		delegate.setApplicationContext(applicationContext);
	}

	@Test
	public void sameContextShouldTriggerStop() {
		when(nacosDiscoveryProperties.getGracefulShutdownWaitTime()).thenReturn(0);

		delegate.onApplicationEvent(new ContextClosedEvent(applicationContext));

		verify(autoServiceRegistration).stop();
	}

	@Test
	public void differentContextShouldNotTriggerStop() {
		delegate.onApplicationEvent(new ContextClosedEvent(Mockito.mock(ApplicationContext.class)));

		verify(autoServiceRegistration, never()).stop();
	}

	@Test
	public void stopExceptionShouldBeSwallowed() {
		doThrow(new RuntimeException("boom")).when(autoServiceRegistration).stop();

		delegate.onApplicationEvent(new ContextClosedEvent(applicationContext));

		verify(autoServiceRegistration).stop();
	}

	@Test
	public void supportsAsyncExecutionShouldBeFalse() {
		assertThat(delegate.supportsAsyncExecution()).isFalse();
	}
}


