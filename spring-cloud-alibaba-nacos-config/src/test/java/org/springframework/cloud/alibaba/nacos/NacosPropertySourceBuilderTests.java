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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySource;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySourceBuilder;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author pbting
 * @date 2019-01-17 11:49 AM
 */
public class NacosPropertySourceBuilderTests extends BaseNacosConfigTests {

	private final static Logger log = LoggerFactory
			.getLogger(NacosPropertySourceBuilderTests.class);

	@Test
	public void nacosPropertySourceBuilder() {

		assertThat(nacosPropertySourceBuilderInstance() != null).isEqualTo(true);
	}

	@Test
	public void getConfigByProperties() {
		NacosPropertySourceBuilder nacosPropertySourceBuilder = nacosPropertySourceBuilderInstance();

		Method method = ReflectionUtils.findMethod(NacosPropertySourceBuilder.class,
				"build", String.class, String.class, String.class, boolean.class);
		ReflectionUtils.makeAccessible(method);
		assertThat(method != null).isEqualTo(true);

		try {
			Object result = method.invoke(nacosPropertySourceBuilder,
					"ext-config-common01.properties", "DEFAULT_GROUP", "properties",
					true);
			assertThat(result != null).isEqualTo(true);
			assertThat(result instanceof NacosPropertySource).isEqualTo(true);
			NacosPropertySource nacosPropertySource = (NacosPropertySource) result;
			// assertThat(nacosPropertySource.getProperty("ext.key"))
			// .isEqualTo("ext.value01");
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getConfigByYaml() {
		NacosPropertySourceBuilder nacosPropertySourceBuilder = nacosPropertySourceBuilderInstance();

		Method method = ReflectionUtils.findMethod(NacosPropertySourceBuilder.class,
				"build", String.class, String.class, String.class, boolean.class);
		ReflectionUtils.makeAccessible(method);
		assertThat(method != null).isEqualTo(true);

		try {
			Object result = method.invoke(nacosPropertySourceBuilder,
					"app-local-common.yaml", "DEFAULT_GROUP", "yaml", true);
			assertThat(result != null).isEqualTo(true);
			assertThat(result instanceof NacosPropertySource).isEqualTo(true);
			NacosPropertySource nacosPropertySource = (NacosPropertySource) result;
			// assertThat(nacosPropertySource.getProperty("app-local-common"))
			// .isEqualTo("update app local shared cguration for Nacos");
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getConfigByYml() {
		NacosPropertySourceBuilder nacosPropertySourceBuilder = nacosPropertySourceBuilderInstance();

		Method method = ReflectionUtils.findMethod(NacosPropertySourceBuilder.class,
				"build", String.class, String.class, String.class, boolean.class);
		ReflectionUtils.makeAccessible(method);
		assertThat(method != null).isEqualTo(true);

		try {
			Object result = method.invoke(nacosPropertySourceBuilder, "nacos.yml",
					"DEFAULT_GROUP", "yml", true);
			assertThat(result != null).isEqualTo(true);
			assertThat(result instanceof NacosPropertySource).isEqualTo(true);
			NacosPropertySource nacosPropertySource = (NacosPropertySource) result;
			// assertThat(nacosPropertySource.getProperty("address"))
			// .isEqualTo("zhejiang-hangzhou");
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
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