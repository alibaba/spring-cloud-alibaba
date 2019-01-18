/*
 * Copyright (C) 2019 the original author or authors.
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

import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.alibaba.nacos.endpoint.NacosConfigHealthIndicator;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author pbting
 * @date 2019-01-17 2:58 PM
 */
public class NacosConfigHealthIndicatorTests extends NacosPowerMockitBaseTests {

	@Test
	public void nacosConfigHealthIndicatorInstance() {
		NacosConfigHealthIndicator nacosConfigHealthIndicator = this.context
				.getBean(NacosConfigHealthIndicator.class);

		assertThat(nacosConfigHealthIndicator != null).isEqualTo(true);
	}

	@Test
	public void testHealthCheck() {

		NacosConfigHealthIndicator nacosConfigHealthIndicator = this.context
				.getBean(NacosConfigHealthIndicator.class);

		Health.Builder builder = Health.up();

		Method method = ReflectionUtils.findMethod(NacosConfigHealthIndicator.class,
				"doHealthCheck", Health.Builder.class);
		ReflectionUtils.makeAccessible(method);
		assertThat(method != null).isEqualTo(true);

		try {
			method.invoke(nacosConfigHealthIndicator, builder);
			assertThat(builder != null).isEqualTo(true);
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}
}