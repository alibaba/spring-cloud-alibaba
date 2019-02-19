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
import org.powermock.api.support.MethodProxy;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySource;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySourceBuilder;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author pbting
 * @date 2019-01-17 11:49 AM
 */
public class NacosPropertySourceBuilderTests extends NacosPowerMockitBaseTests {

	@Test
	public void nacosPropertySourceBuilder() {

		assertThat(nacosPropertySourceBuilderInstance() != null).isEqualTo(true);
	}

	@Test
	public void getConfigByProperties() {
		try {
			final HashMap<String, String> value = new HashMap<>();
			value.put("dev.mode", "local-mock");

			final Constructor constructor = ReflectionUtils.accessibleConstructor(
					NacosPropertySource.class, String.class, String.class, Map.class,
					Date.class, boolean.class);

			NacosPropertySourceBuilder nacosPropertySourceBuilder = nacosPropertySourceBuilderInstance();

			Method method = ReflectionUtils.findMethod(NacosPropertySourceBuilder.class,
					"build", String.class, String.class, String.class, boolean.class);
			ReflectionUtils.makeAccessible(method);
			assertThat(method != null).isEqualTo(true);
			MethodProxy.proxy(method, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					Object instance = constructor.newInstance(args[1].toString(),
							args[0].toString(), value, new Date(), args[3]);
					return instance;
				}
			});

			Object result = method.invoke(nacosPropertySourceBuilder,
					"mock-nacos-config.properties", "DEFAULT_GROUP", "properties", true);
			assertThat(result != null).isEqualTo(true);
			assertThat(result instanceof NacosPropertySource).isEqualTo(true);
			NacosPropertySource nacosPropertySource = (NacosPropertySource) result;
			assertThat(nacosPropertySource.getProperty("dev.mode"))
					.isEqualTo("local-mock");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getConfigByYaml() {

		try {
			//
			final HashMap<String, String> value = new HashMap<>();
			value.put("mock-ext-config", "mock-ext-config-value");

			final Constructor constructor = ReflectionUtils.accessibleConstructor(
					NacosPropertySource.class, String.class, String.class, Map.class,
					Date.class, boolean.class);

			Method method = ReflectionUtils.findMethod(NacosPropertySourceBuilder.class,
					"build", String.class, String.class, String.class, boolean.class);
			ReflectionUtils.makeAccessible(method);
			assertThat(method != null).isEqualTo(true);

			MethodProxy.proxy(method, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					Object instance = constructor.newInstance(args[1].toString(),
							args[0].toString(), value, new Date(), args[3]);
					return instance;
				}
			});

			NacosPropertySourceBuilder nacosPropertySourceBuilder = nacosPropertySourceBuilderInstance();
			Object result = method.invoke(nacosPropertySourceBuilder, "ext-config.yaml",
					"DEFAULT_GROUP", "yaml", true);
			assertThat(result != null).isEqualTo(true);
			assertThat(result instanceof NacosPropertySource).isEqualTo(true);
			NacosPropertySource nacosPropertySource = (NacosPropertySource) result;
			assertThat(nacosPropertySource.getProperty("mock-ext-config"))
					.isEqualTo("mock-ext-config-value");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getConfigByYml() {
		try {
			//
			final HashMap<String, String> value = new HashMap<>();
			value.put("mock-ext-config-yml", "mock-ext-config-yml-value");

			final Constructor constructor = ReflectionUtils.accessibleConstructor(
					NacosPropertySource.class, String.class, String.class, Map.class,
					Date.class, boolean.class);

			Method method = ReflectionUtils.findMethod(NacosPropertySourceBuilder.class,
					"build", String.class, String.class, String.class, boolean.class);
			ReflectionUtils.makeAccessible(method);
			assertThat(method != null).isEqualTo(true);

			MethodProxy.proxy(method, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					Object instance = constructor.newInstance(args[1].toString(),
							args[0].toString(), value, new Date(), args[3]);
					return instance;
				}
			});

			NacosPropertySourceBuilder nacosPropertySourceBuilder = nacosPropertySourceBuilderInstance();
			Object result = method.invoke(nacosPropertySourceBuilder, "ext-config.yml",
					"DEFAULT_GROUP", "yml", true);
			assertThat(result != null).isEqualTo(true);
			assertThat(result instanceof NacosPropertySource).isEqualTo(true);
			NacosPropertySource nacosPropertySource = (NacosPropertySource) result;
			assertThat(nacosPropertySource.getProperty("mock-ext-config-yml"))
					.isEqualTo("mock-ext-config-yml-value");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getEmpty() {
		NacosPropertySourceBuilder nacosPropertySourceBuilder = nacosPropertySourceBuilderInstance();

		Method method = ReflectionUtils.findMethod(NacosPropertySourceBuilder.class,
				"build", String.class, String.class, String.class, boolean.class);
		ReflectionUtils.makeAccessible(method);
		assertThat(method != null).isEqualTo(true);

		try {
			Object result = method.invoke(nacosPropertySourceBuilder, "nacos-empty.yml",
					"DEFAULT_GROUP", "yml", true);
			assertThat(result != null).isEqualTo(true);
			assertThat(result instanceof NacosPropertySource).isEqualTo(true);
			NacosPropertySource nacosPropertySource = (NacosPropertySource) result;
			assertThat(nacosPropertySource.getProperty("address")).isEqualTo(null);
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}